package com.olie.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.note.NoteRequest;
import com.olie.api.dto.note.NoteResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.note.CreateNoteUseCase;
import com.olie.api.usecase.note.DeleteNoteUseCase;
import com.olie.api.usecase.note.ListNotesUseCase;
import com.olie.api.usecase.note.UpdateNoteUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/notes")
@RequiredArgsConstructor
public class NoteController {

    private final CreateNoteUseCase createNoteUseCase;
    private final UpdateNoteUseCase updateNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;
    private final ListNotesUseCase listNotesUseCase;

    @GetMapping
    public List<NoteResponse> list(@AuthenticationPrincipal User user) {
        return listNotesUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody NoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createNoteUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public NoteResponse update(
            @AuthenticationPrincipal User user, @PathVariable UUID id, @Valid @RequestBody NoteRequest request) {
        return updateNoteUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteNoteUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }
}
