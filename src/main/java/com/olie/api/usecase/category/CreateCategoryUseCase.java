package com.olie.api.usecase.category;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.category.CategoryRequest;
import com.olie.api.dto.category.CategoryResponse;
import com.olie.api.entity.Category;
import com.olie.api.entity.User;
import com.olie.api.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CategoryResponse execute(User user, CategoryRequest request) {
        Category parent = resolveParent(user, request.parentId());

        Category category = Category.builder()
                .name(request.name())
                .user(user)
                .parent(parent)
                .build();

        categoryRepository.save(category);

        return CategoryResponse.from(category);
    }

    private Category resolveParent(User user, UUID parentId) {
        if (parentId == null) {
            return null;
        }

        Category parent = categoryRepository.findByIdAndUserId(parentId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));

        if (parent.getParent() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A subcategory cannot have subcategories");
        }

        return parent;
    }
}
