package com.olie.api.dto.task;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(@NotBlank String title, String description) {
}
