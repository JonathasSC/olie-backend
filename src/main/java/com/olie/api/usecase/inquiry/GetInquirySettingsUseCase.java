package com.olie.api.usecase.inquiry;

import org.springframework.stereotype.Service;

import com.olie.api.dto.inquiry.InquirySettingsResponse;
import com.olie.api.inquiry.InquiryProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInquirySettingsUseCase {

    private final InquiryProperties properties;

    public InquirySettingsResponse execute() {
        return new InquirySettingsResponse(
                properties.messageTemplate(),
                properties.timezone().getId(),
                properties.maxItems(),
                properties.maxRecipients(),
                properties.contactIntervalMin().toSeconds(),
                properties.contactIntervalMax().toSeconds(),
                properties.duplicateWindow().toHours(),
                properties.photos().maxUploadSize().toBytes());
    }
}
