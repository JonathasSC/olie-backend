package com.olie.api.dto.contact;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ContactRequest(
        @NotBlank String name,
        @NotBlank String phone,
        @NotEmpty Set<UUID> categoryIds,
        Boolean active,
        Boolean doNotContact) {
}
