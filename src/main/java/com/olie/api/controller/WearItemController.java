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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.wearitem.ReplaceWearItemRequest;
import com.olie.api.dto.wearitem.WearItemDetailResponse;
import com.olie.api.dto.wearitem.WearItemRequest;
import com.olie.api.dto.wearitem.WearItemResponse;
import com.olie.api.entity.User;
import com.olie.api.entity.WearItemStatus;
import com.olie.api.usecase.wearitem.CreateWearItemUseCase;
import com.olie.api.usecase.wearitem.DeleteWearItemUseCase;
import com.olie.api.usecase.wearitem.GetWearItemUseCase;
import com.olie.api.usecase.wearitem.ListWearItemsUseCase;
import com.olie.api.usecase.wearitem.ReplaceWearItemUseCase;
import com.olie.api.usecase.wearitem.UpdateWearItemUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/wear-items")
@RequiredArgsConstructor
public class WearItemController {

    private final CreateWearItemUseCase createWearItemUseCase;
    private final UpdateWearItemUseCase updateWearItemUseCase;
    private final DeleteWearItemUseCase deleteWearItemUseCase;
    private final ListWearItemsUseCase listWearItemsUseCase;
    private final GetWearItemUseCase getWearItemUseCase;
    private final ReplaceWearItemUseCase replaceWearItemUseCase;

    @GetMapping
    public List<WearItemResponse> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) List<WearItemStatus> status,
            @RequestParam(required = false) UUID categoryId) {
        return listWearItemsUseCase.execute(user, status, categoryId);
    }

    @GetMapping("/{id}")
    public WearItemDetailResponse get(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        return getWearItemUseCase.execute(user, id);
    }

    @PostMapping
    public ResponseEntity<WearItemResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody WearItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createWearItemUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public WearItemResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody WearItemRequest request) {
        return updateWearItemUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteWearItemUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/replace")
    public WearItemResponse replace(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody ReplaceWearItemRequest request) {
        return replaceWearItemUseCase.execute(user, id, request);
    }
}
