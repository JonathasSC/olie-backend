package com.olie.api.usecase.note;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olie.api.dto.note.NoteResponse;
import com.olie.api.entity.User;
import com.olie.api.repository.NoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListNotesUseCase {

    private final NoteRepository noteRepository;

    public List<NoteResponse> execute(User user) {
        return noteRepository.findAllByUserId(user.getId()).stream()
                .map(NoteResponse::from)
                .toList();
    }
}
