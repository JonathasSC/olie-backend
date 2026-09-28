package com.olie.api.dto.inquiry;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryPauseReason;
import com.olie.api.entity.InquiryStatus;

public record InquiryResponse(
        UUID id,
        InquiryStatus status,
        InquiryPauseReason pauseReason,
        boolean simulated,
        List<UUID> categoryIds,
        List<InquiryItemResponse> items,
        List<InquiryRecipientResponse> recipients,
        InquiryTotals totals,
        Instant nextSendAt,
        Instant startedAt,
        Instant finishedAt,
        Instant createdAt) {

    public static InquiryResponse from(Inquiry inquiry) {
        return new InquiryResponse(
                inquiry.getId(),
                inquiry.getStatus(),
                inquiry.getPauseReason(),
                inquiry.isSimulated(),
                inquiry.getCategories().stream().map(ContactCategory::getId).toList(),
                inquiry.getItems().stream().map(InquiryItemResponse::from).toList(),
                inquiry.getRecipients().stream().map(InquiryRecipientResponse::from).toList(),
                InquiryTotals.of(inquiry.getRecipients()),
                inquiry.getNextSendAt(),
                inquiry.getStartedAt(),
                inquiry.getFinishedAt(),
                inquiry.getCreatedAt());
    }
}
