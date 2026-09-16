package com.olie.api.dto.note;

import jakarta.validation.constraints.NotBlank;

public record NoteRequest(@NotBlank String content) {
}
