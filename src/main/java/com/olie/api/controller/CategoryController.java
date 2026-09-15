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

import com.olie.api.dto.category.CategoryRequest;
import com.olie.api.dto.category.CategoryResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.category.CreateCategoryUseCase;
import com.olie.api.usecase.category.DeleteCategoryUseCase;
import com.olie.api.usecase.category.ListCategoriesUseCase;
import com.olie.api.usecase.category.UpdateCategoryUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;

    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal User user) {
        // TODO: autenticação desativada temporariamente — user pode vir null, reavaliar quando reativar
        if (user == null) {
            return List.of();
        }
        return listCategoriesUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createCategoryUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public CategoryResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return updateCategoryUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteCategoryUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }
}
