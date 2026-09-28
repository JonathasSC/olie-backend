package com.olie.api.inquiry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryEvent;
import com.olie.api.dto.inquiry.InquiryTotals;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryItem;
import com.olie.api.entity.InquiryPauseReason;
import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryRecipientStatus;
import com.olie.api.entity.InquiryStatus;
import com.olie.api.notification.NotificationEvent;
import com.olie.api.notification.NotificationEventProducer;
import com.olie.api.repository.InquiryRecipientRepository;
import com.olie.api.repository.InquiryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Passos transacionais do disparo. O envio em si (lento) fica no {@link InquiryDispatcher}, fora de transação. */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquirySendingService {

    static final String INTERRUPTED_REASON =
            "Envio interrompido por reinício do sistema; confira no WhatsApp antes de reenviar.";

    private final InquiryRepository inquiryRepository;
    private final InquiryRecipientRepository inquiryRecipientRepository;
    private final InquiryMessageComposer inquiryMessageComposer;
    private final InquiryEventPublisher inquiryEventPublisher;
    private final NotificationEventProducer notificationEventProducer;
    private final InquiryProperties properties;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<UUID> dueInquiryIds() {
        return inquiryRepository.findIdsDue(InquiryStatus.SENDING, Instant.now(clock));
    }

    /**
     * Um contato em "enviando" na subida do sistema foi interrompido no meio: não dá para saber se recebeu.
     * Em vez de arriscar mandar em dobro, vira falha — o usuário decide se reenvia.
     */
    @Transactional
    public void recoverInterrupted() {
        List<InquiryRecipient> interrupted = inquiryRecipientRepository.findAllByStatus(InquiryRecipientStatus.SENDING);
        for (InquiryRecipient recipient : interrupted) {
            recipient.setStatus(InquiryRecipientStatus.FAILED);
            recipient.setFailureReason(INTERRUPTED_REASON);
            finishIfDone(recipient.getInquiry());
        }
        if (!interrupted.isEmpty()) {
            log.warn("{} envio(s) interrompido(s) marcados como falha", interrupted.size());
        }
    }

    /** Separa o próximo contato da fila; sem ninguém pendente, conclui a consulta. */
    @Transactional
    public Optional<SendJob> startNextRecipient(UUID inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElse(null);
        if (inquiry == null || inquiry.getStatus() != InquiryStatus.SENDING) {
            return Optional.empty();
        }

        Optional<InquiryRecipient> next = inquiry.getRecipients().stream()
                .filter(recipient -> recipient.getStatus() == InquiryRecipientStatus.PENDING)
                .findFirst();
        if (next.isEmpty()) {
            finishIfDone(inquiry);
            return Optional.empty();
        }

        InquiryRecipient recipient = next.get();
        List<String> itemNames = inquiry.getItems().stream().map(InquiryItem::getName).toList();
        ComposedMessage message = inquiryMessageComposer.compose(itemNames);

        recipient.setStatus(InquiryRecipientStatus.SENDING);
        // numa retomada o texto já enviado é mantido: é o que o contato de fato recebeu
        if (recipient.getSentParts() == 0) {
            recipient.setMessageText(message.text());
        }
        inquiry.setNextSendAt(null);

        inquiryEventPublisher.publish(inquiry.getUser().getEmail(),
                InquiryEvent.of(InquiryEvent.Type.RECIPIENT_UPDATED, inquiry, recipient));

        return Optional.of(new SendJob(inquiry.getId(), recipient.getId(), inquiry.getUser().getId(),
                recipient.getPhone(), inquiry.isSimulated(), parts(inquiry, recipient, message),
                recipient.getSentParts()));
    }

    @Transactional
    public void recordPartSent(UUID recipientId, int sentParts) {
        inquiryRecipientRepository.findById(recipientId).ifPresent(recipient -> recipient.setSentParts(sentParts));
    }

    @Transactional
    public void finishRecipient(SendJob job, SendOutcome outcome) {
        InquiryRecipient recipient = inquiryRecipientRepository.findById(job.recipientId()).orElse(null);
        if (recipient == null) {
            return;
        }
        Inquiry inquiry = recipient.getInquiry();
        recipient.setAttempts(recipient.getAttempts() + outcome.attempts());

        switch (outcome.result()) {
            case SENT -> {
                recipient.setStatus(InquiryRecipientStatus.SENT);
                recipient.setFailureReason(null);
                recipient.setSentAt(Instant.now(clock));
            }
            case FAILED -> {
                recipient.setStatus(InquiryRecipientStatus.FAILED);
                recipient.setFailureReason(outcome.failureReason());
            }
            case CONNECTION_LOST -> {
                recipient.setStatus(InquiryRecipientStatus.PENDING);
                if (inquiry.getStatus() == InquiryStatus.SENDING) {
                    inquiry.setStatus(InquiryStatus.PAUSED);
                    inquiry.setPauseReason(InquiryPauseReason.CONNECTION_LOST);
                }
            }
        }

        // se o usuário cancelou enquanto este contato era processado, ele fica como terminou
        if (inquiry.getStatus() == InquiryStatus.CANCELLED && recipient.getStatus() == InquiryRecipientStatus.PENDING) {
            recipient.setStatus(InquiryRecipientStatus.CANCELLED);
        }

        String email = inquiry.getUser().getEmail();
        inquiryEventPublisher.publish(email, InquiryEvent.of(InquiryEvent.Type.RECIPIENT_UPDATED, inquiry, recipient));

        if (!finishIfDone(inquiry) && inquiry.getStatus() == InquiryStatus.SENDING) {
            inquiry.setNextSendAt(Instant.now(clock).plus(nextInterval(inquiry.isSimulated())));
            inquiryEventPublisher.publish(email, InquiryEvent.of(InquiryEvent.Type.INQUIRY_UPDATED, inquiry, null));
        } else if (inquiry.getStatus() == InquiryStatus.PAUSED) {
            inquiryEventPublisher.publish(email, InquiryEvent.of(InquiryEvent.Type.INQUIRY_UPDATED, inquiry, null));
        }
    }

    /** Conclui a consulta quando não resta ninguém aguardando nem em envio. */
    private boolean finishIfDone(Inquiry inquiry) {
        boolean remaining = inquiry.getRecipients().stream().anyMatch(recipient ->
                recipient.getStatus() == InquiryRecipientStatus.PENDING
                        || recipient.getStatus() == InquiryRecipientStatus.SENDING);
        if (remaining || inquiry.getStatus() != InquiryStatus.SENDING) {
            return false;
        }

        inquiry.setStatus(InquiryStatus.COMPLETED);
        inquiry.setPauseReason(null);
        inquiry.setNextSendAt(null);
        inquiry.setFinishedAt(Instant.now(clock));

        inquiryEventPublisher.publish(inquiry.getUser().getEmail(),
                InquiryEvent.of(InquiryEvent.Type.INQUIRY_FINISHED, inquiry, null));
        if (!inquiry.isSimulated()) {
            InquiryTotals totals = InquiryTotals.of(inquiry.getRecipients());
            notificationEventProducer.publish(
                    NotificationEvent.inquiryFinished(inquiry.getUser(), totals.sent(), totals.failed()));
        }
        return true;
    }

    private Duration nextInterval(boolean simulated) {
        if (simulated) {
            return properties.simulationInterval();
        }
        long min = properties.contactIntervalMin().toMillis();
        long max = properties.contactIntervalMax().toMillis();
        return Duration.ofMillis(min == max ? min : ThreadLocalRandom.current().nextLong(min, max + 1));
    }

    private static List<SendJob.Part> parts(Inquiry inquiry, InquiryRecipient recipient, ComposedMessage message) {
        List<SendJob.Part> parts = new ArrayList<>();
        parts.add(new SendJob.Part(recipient.getMessageText(), null, null));
        for (InquiryItem item : inquiry.getItems()) {
            if (item.getPhoto() != null) {
                parts.add(new SendJob.Part(message.itemCaptions().get(item.getPosition()),
                        item.getPhoto().getStoragePath(), item.getPhoto().getContentType()));
            }
        }
        return parts;
    }
}
