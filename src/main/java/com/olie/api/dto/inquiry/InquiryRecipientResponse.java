package com.olie.api.dto.inquiry;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryRecipientStatus;

public record InquiryRecipientResponse(
        UUID id,
        UUID contactId,
        String contactName,
        String phone,
        InquiryRecipientStatus status,
        String failureReason,
        int attempts,
        Instant sentAt,
        String messageText) {

    public static InquiryRecipientResponse from(InquiryRecipient recipient) {
        return new InquiryRecipientResponse(
                recipient.getId(),
                recipient.getContact() != null ? recipient.getContact().getId() : null,
                recipient.getContactName(),
                recipient.getPhone(),
                recipient.getStatus(),
                recipient.getFailureReason(),
                recipient.getAttempts(),
                recipient.getSentAt(),
                recipient.getMessageText());
    }
}
