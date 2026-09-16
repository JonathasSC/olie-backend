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

import com.olie.api.dto.planneditem.CompletePlannedItemRequest;
import com.olie.api.dto.planneditem.PlannedItemRequest;
import com.olie.api.dto.planneditem.PlannedItemResponse;
import com.olie.api.dto.transaction.TransactionResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.planneditem.CompletePlannedItemUseCase;
import com.olie.api.usecase.planneditem.CreatePlannedItemUseCase;
import com.olie.api.usecase.planneditem.DeletePlannedItemUseCase;
import com.olie.api.usecase.planneditem.ListPlannedItemsUseCase;
import com.olie.api.usecase.planneditem.UpdatePlannedItemUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/planned-items")
@RequiredArgsConstructor
public class PlannedItemController {

    private final CreatePlannedItemUseCase createPlannedItemUseCase;
    private final UpdatePlannedItemUseCase updatePlannedItemUseCase;
    private final DeletePlannedItemUseCase deletePlannedItemUseCase;
    private final ListPlannedItemsUseCase listPlannedItemsUseCase;
    private final CompletePlannedItemUseCase completePlannedItemUseCase;

    @GetMapping
    public List<PlannedItemResponse> list(@AuthenticationPrincipal User user) {
        return listPlannedItemsUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<PlannedItemResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody PlannedItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createPlannedItemUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public PlannedItemResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody PlannedItemRequest request) {
        return updatePlannedItemUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deletePlannedItemUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/complete")
    public TransactionResponse complete(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody CompletePlannedItemRequest request) {
        return completePlannedItemUseCase.execute(user, id, request);
    }
}
