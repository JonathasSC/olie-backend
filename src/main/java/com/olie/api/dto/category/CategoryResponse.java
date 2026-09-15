package com.olie.api.dto.category;

import java.util.UUID;

import com.olie.api.entity.Category;

public record CategoryResponse(UUID id, String name, UUID parentId) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null);
    }
}
