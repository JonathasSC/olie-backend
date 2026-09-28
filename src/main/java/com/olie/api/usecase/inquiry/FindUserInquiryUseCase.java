package com.olie.api.usecase.inquiry;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.Inquiry;
import com.olie.api.entity.User;
import com.olie.api.repository.InquiryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindUserInquiryUseCase {

    private final InquiryRepository inquiryRepository;

    public Inquiry execute(User user, UUID inquiryId) {
        return inquiryRepository.findByIdAndUserId(inquiryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquiry not found"));
    }
}
