package com.olie.api.dto.note;

import java.time.Instant;
import java.util.UUID;

import com.olie.api.entity.Note;

public record NoteResponse(UUID id, String content, Instant createdAt, Instant updatedAt) {

    public static NoteResponse from(Note note) {
        return new NoteResponse(note.getId(), note.getContent(), note.getCreatedAt(), note.getUpdatedAt());
    }
}
