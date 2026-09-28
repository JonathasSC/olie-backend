package com.olie.api.dto.inquiry;

import java.util.List;

import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryRecipientStatus;

public record InquiryTotals(long total, long pending, long sending, long sent, long failed, long cancelled) {

    public static InquiryTotals of(List<InquiryRecipient> recipients) {
        return new InquiryTotals(
                recipients.size(),
                count(recipients, InquiryRecipientStatus.PENDING),
                count(recipients, InquiryRecipientStatus.SENDING),
                count(recipients, InquiryRecipientStatus.SENT),
                count(recipients, InquiryRecipientStatus.FAILED),
                count(recipients, InquiryRecipientStatus.CANCELLED));
    }

    private static long count(List<InquiryRecipient> recipients, InquiryRecipientStatus status) {
        return recipients.stream().filter(recipient -> recipient.getStatus() == status).count();
    }
}
