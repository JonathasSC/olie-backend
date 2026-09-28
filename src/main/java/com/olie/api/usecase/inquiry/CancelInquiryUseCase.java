package com.olie.api.usecase.inquiry;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryRecipientStatus;
import com.olie.api.entity.InquiryStatus;
import com.olie.api.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CancelInquiryUseCase {

    private final FindUserInquiryUseCase findUserInquiryUseCase;
    private final InquiryControlSupport inquiryControlSupport;

    /** Contatos ainda na fila são cancelados; quem está recebendo neste instante termina normalmente. */
    @Transactional
    public InquiryResponse execute(User user, UUID inquiryId) {
        Inquiry inquiry = findUserInquiryUseCase.execute(user, inquiryId);
        inquiryControlSupport.requireStatus(inquiry, InquiryStatus.SENDING, InquiryStatus.PAUSED);

        inquiry.getRecipients().stream()
                .filter(recipient -> recipient.getStatus() == InquiryRecipientStatus.PENDING)
                .forEach(recipient -> recipient.setStatus(InquiryRecipientStatus.CANCELLED));
        inquiry.setStatus(InquiryStatus.CANCELLED);
        inquiry.setPauseReason(null);
        inquiry.setNextSendAt(null);
        inquiry.setFinishedAt(inquiryControlSupport.now());

        return inquiryControlSupport.publish(user, inquiry);
    }
}
