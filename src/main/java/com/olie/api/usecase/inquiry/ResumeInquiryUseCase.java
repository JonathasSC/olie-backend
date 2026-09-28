package com.olie.api.usecase.inquiry;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryStatus;
import com.olie.api.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResumeInquiryUseCase {

    private final FindUserInquiryUseCase findUserInquiryUseCase;
    private final InquiryControlSupport inquiryControlSupport;

    @Transactional
    public InquiryResponse execute(User user, UUID inquiryId) {
        Inquiry inquiry = findUserInquiryUseCase.execute(user, inquiryId);
        inquiryControlSupport.requireStatus(inquiry, InquiryStatus.PAUSED);
        inquiryControlSupport.requireConnected(user, inquiry);

        inquiryControlSupport.restart(inquiry);

        return inquiryControlSupport.publish(user, inquiry);
    }
}
