package com.olie.api.dto.contact;

import jakarta.validation.constraints.NotBlank;

public record ContactCategoryRequest(@NotBlank String name) {
}
