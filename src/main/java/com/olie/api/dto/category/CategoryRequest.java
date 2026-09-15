package com.olie.api.dto.category;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, UUID parentId) {
}
