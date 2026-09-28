package com.olie.api.dto.inquiry;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record InquiryItemRequest(@NotBlank String name, UUID photoId) {
}
