package com.olie.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.task.BoardResponse;
import com.olie.api.dto.task.MoveTaskRequest;
import com.olie.api.dto.task.TaskRequest;
import com.olie.api.dto.task.TaskResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.task.CreateTaskUseCase;
import com.olie.api.usecase.task.DeleteTaskUseCase;
import com.olie.api.usecase.task.ListTasksUseCase;
import com.olie.api.usecase.task.MoveTaskUseCase;
import com.olie.api.usecase.task.UpdateTaskUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final UpdateTaskUseCase updateTaskUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final ListTasksUseCase listTasksUseCase;
    private final MoveTaskUseCase moveTaskUseCase;

    @GetMapping
    public BoardResponse board(@AuthenticationPrincipal User user) {
        // TODO: autenticação desativada temporariamente — user pode vir null, reavaliar quando reativar
        if (user == null) {
            return new BoardResponse(List.of(), List.of(), List.of());
        }
        return listTasksUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createTaskUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @AuthenticationPrincipal User user, @PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
        return updateTaskUseCase.execute(user, id, request);
    }

    @PatchMapping("/{id}/move")
    public TaskResponse move(
            @AuthenticationPrincipal User user, @PathVariable UUID id, @Valid @RequestBody MoveTaskRequest request) {
        return moveTaskUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteTaskUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }
}
