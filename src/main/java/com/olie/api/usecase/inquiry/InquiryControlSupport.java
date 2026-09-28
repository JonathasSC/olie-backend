package com.olie.api.usecase.inquiry;

import java.time.Clock;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.olie.api.dto.inquiry.InquiryEvent;
import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryStatus;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.inquiry.InquiryEventPublisher;
import com.olie.api.whatsapp.WhatsAppProvider;

import lombok.RequiredArgsConstructor;

/** Regras comuns aos controles do envio (pausar, retomar, cancelar, reenviar falhas). */
@Component
@RequiredArgsConstructor
public class InquiryControlSupport {

    private final InquiryEventPublisher inquiryEventPublisher;
    private final WhatsAppProvider whatsAppProvider;
    private final Clock clock;

    public void requireStatus(Inquiry inquiry, InquiryStatus... allowed) {
        for (InquiryStatus status : allowed) {
            if (inquiry.getStatus() == status) {
                return;
            }
        }
        throw new ApiException(HttpStatus.CONFLICT, "INVALID_INQUIRY_STATUS",
                "Ação indisponível para uma consulta com status %s.".formatted(inquiry.getStatus()));
    }

    public void requireConnected(User user, Inquiry inquiry) {
        if (!inquiry.isSimulated() && !whatsAppProvider.connection(user.getId()).isConnected()) {
            throw new ApiException(HttpStatus.CONFLICT, "WHATSAPP_NOT_CONNECTED",
                    "Conecte o WhatsApp antes de retomar o envio.");
        }
    }

    /** Volta a consulta para a fila, com o próximo contato liberado imediatamente. */
    public void restart(Inquiry inquiry) {
        inquiry.setStatus(InquiryStatus.SENDING);
        inquiry.setPauseReason(null);
        inquiry.setNextSendAt(Instant.now(clock));
    }

    public Instant now() {
        return Instant.now(clock);
    }

    public InquiryResponse publish(User user, Inquiry inquiry) {
        inquiryEventPublisher.publish(user.getEmail(), InquiryEvent.of(InquiryEvent.Type.INQUIRY_UPDATED, inquiry, null));
        return InquiryResponse.from(inquiry);
    }
}
