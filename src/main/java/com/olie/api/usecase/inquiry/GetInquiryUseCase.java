package com.olie.api.usecase.inquiry;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInquiryUseCase {

    private final FindUserInquiryUseCase findUserInquiryUseCase;

    @Transactional(readOnly = true)
    public InquiryResponse execute(User user, UUID inquiryId) {
        return InquiryResponse.from(findUserInquiryUseCase.execute(user, inquiryId));
    }
}
