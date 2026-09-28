package com.olie.api.inquiry;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.olie.api.whatsapp.SimulatedWhatsAppProvider;
import com.olie.api.whatsapp.WhatsAppProvider;
import com.olie.api.whatsapp.WhatsAppSendException;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * Fila de envio. Uma thread própria (não a do agendador compartilhado, que também atende o WebSocket) consulta
 * a cada segundo as consultas cujo próximo envio venceu e processa um contato de cada. Todo o estado fica no
 * banco, então uma queda do sistema retoma do primeiro contato ainda não enviado.
 */
@Slf4j
@Component
public class InquiryDispatcher {

    private static final Duration TICK = Duration.ofSeconds(1);

    private final InquirySendingService sendingService;
    private final WhatsAppProvider whatsAppProvider;
    private final WhatsAppProvider simulatedProvider = new SimulatedWhatsAppProvider();
    private final InquiryPhotoStorage photoStorage;
    private final InquiryProperties properties;

    private ScheduledExecutorService executor;

    public InquiryDispatcher(InquirySendingService sendingService, WhatsAppProvider whatsAppProvider,
            InquiryPhotoStorage photoStorage, InquiryProperties properties) {
        this.sendingService = sendingService;
        this.whatsAppProvider = whatsAppProvider;
        this.photoStorage = photoStorage;
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        sendingService.recoverInterrupted();

        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "inquiry-dispatcher");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(this::tick, TICK.toMillis(), TICK.toMillis(), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    void tick() {
        try {
            List<UUID> due = sendingService.dueInquiryIds();
            for (UUID inquiryId : due) {
                sendingService.startNextRecipient(inquiryId).ifPresent(job -> {
                    SendOutcome outcome = send(job);
                    sendingService.finishRecipient(job, outcome);
                });
            }
        } catch (RuntimeException exception) {
            // nunca deixa a exceção escapar: o ScheduledExecutorService cancelaria as próximas execuções
            log.error("Falha no disparador de consultas", exception);
        }
    }

    SendOutcome send(SendJob job) {
        WhatsAppProvider provider = job.simulated() ? simulatedProvider : whatsAppProvider;
        int attempts = 0;
        int sentParts = job.sentParts();

        while (true) {
            attempts++;
            try {
                for (int index = sentParts; index < job.parts().size(); index++) {
                    if (index > 0 && !job.simulated()) {
                        sleep(properties.messageInterval());
                    }
                    sendPart(provider, job, job.parts().get(index));
                    sentParts = index + 1;
                    sendingService.recordPartSent(job.recipientId(), sentParts);
                }
                return SendOutcome.sent(attempts);
            } catch (WhatsAppSendException exception) {
                switch (exception.getKind()) {
                    case NOT_CONNECTED -> {
                        return SendOutcome.connectionLost(attempts);
                    }
                    case PERMANENT -> {
                        return SendOutcome.failed(attempts, exception.getMessage());
                    }
                    case TRANSIENT -> {
                        if (attempts >= properties.maxAttempts()) {
                            return SendOutcome.failed(attempts,
                                    "%s (após %d tentativas)".formatted(exception.getMessage(), attempts));
                        }
                        log.info("Falha transitória ao enviar para {} (tentativa {}): {}",
                                job.phone(), attempts, exception.getMessage());
                        sleep(properties.retryDelay().multipliedBy(attempts));
                    }
                }
            } catch (RuntimeException exception) {
                log.error("Erro inesperado ao enviar para {}", job.phone(), exception);
                return SendOutcome.failed(attempts, "Erro interno: " + exception.getMessage());
            }
        }
    }

    private void sendPart(WhatsAppProvider provider, SendJob job, SendJob.Part part) {
        if (part.isPhoto()) {
            provider.sendImage(job.userId(), job.phone(), photoStorage.read(part.photoPath()),
                    part.photoContentType(), part.text());
        } else {
            provider.sendText(job.userId(), job.phone(), part.text());
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Disparador interrompido", exception);
        }
    }
}
