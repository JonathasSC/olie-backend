package com.olie.api.usecase.note;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.note.NoteRequest;
import com.olie.api.dto.note.NoteResponse;
import com.olie.api.entity.Note;
import com.olie.api.entity.User;
import com.olie.api.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateNoteUseCase {

    private final NoteRepository noteRepository;

    public NoteResponse execute(User user, UUID noteId, NoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        note.setContent(request.content());
        note.setUpdatedAt(Instant.now());

        noteRepository.save(note);

        return NoteResponse.from(note);
    }
}
