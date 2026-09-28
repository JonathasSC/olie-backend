package com.olie.api.dto.inquiry;

import java.time.Instant;
import java.util.UUID;

public record DuplicateInquiryWarning(UUID contactId, String contactName, String itemName, Instant sentAt) {
}
