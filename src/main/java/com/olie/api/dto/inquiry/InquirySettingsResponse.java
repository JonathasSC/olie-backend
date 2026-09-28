package com.olie.api.dto.inquiry;

public record InquirySettingsResponse(
        String messageTemplate,
        String timezone,
        int maxItems,
        int maxRecipients,
        long contactIntervalMinSeconds,
        long contactIntervalMaxSeconds,
        long duplicateWindowHours,
        long maxPhotoUploadBytes) {
}
