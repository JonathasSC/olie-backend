package com.olie.api.usecase.note;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.Note;
import com.olie.api.entity.User;
import com.olie.api.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteNoteUseCase {

    private final NoteRepository noteRepository;

    public void execute(User user, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        noteRepository.delete(note);
    }
}
