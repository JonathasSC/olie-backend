package com.olie.api.usecase.note;

import org.springframework.stereotype.Service;

import com.olie.api.dto.note.NoteRequest;
import com.olie.api.dto.note.NoteResponse;
import com.olie.api.entity.Note;
import com.olie.api.entity.User;
import com.olie.api.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateNoteUseCase {

    private final NoteRepository noteRepository;

    public NoteResponse execute(User user, NoteRequest request) {
        Note note = Note.builder()
                .user(user)
                .content(request.content())
                .build();

        noteRepository.save(note);

        return NoteResponse.from(note);
    }
}
