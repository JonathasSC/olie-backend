package com.olie.api.dto.inquiry;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record PreviewInquiryRequest(@NotEmpty List<@Valid InquiryItemRequest> items) {
}
