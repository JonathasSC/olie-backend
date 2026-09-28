package com.olie.api.dto.inquiry;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryPauseReason;
import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryStatus;

/** Evento em tempo real enviado ao usuário em {@code /user/queue/inquiries}. */
public record InquiryEvent(
        Type type,
        UUID inquiryId,
        InquiryStatus status,
        InquiryPauseReason pauseReason,
        boolean simulated,
        Instant nextSendAt,
        InquiryTotals totals,
        InquiryRecipientResponse recipient) {

    public enum Type {
        /** Mudou o status de um contato (aguardando → enviando → enviado/falhou). */
        RECIPIENT_UPDATED,
        /** Mudou o estado da consulta (pausa, retomada, cancelamento, próximo envio agendado). */
        INQUIRY_UPDATED,
        /** Terminou: {@code totals} traz o resumo final. */
        INQUIRY_FINISHED
    }

    public static InquiryEvent of(Type type, Inquiry inquiry, InquiryRecipient recipient) {
        return new InquiryEvent(
                type,
                inquiry.getId(),
                inquiry.getStatus(),
                inquiry.getPauseReason(),
                inquiry.isSimulated(),
                inquiry.getNextSendAt(),
                InquiryTotals.of(inquiry.getRecipients()),
                recipient != null ? InquiryRecipientResponse.from(recipient) : null);
    }
}
