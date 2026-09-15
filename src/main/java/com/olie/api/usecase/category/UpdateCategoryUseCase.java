package com.olie.api.usecase.category;

import java.time.Instant;
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
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CategoryResponse execute(User user, UUID categoryId, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Category parent = resolveParent(user, category, request.parentId());

        category.setName(request.name());
        category.setParent(parent);
        category.setUpdatedAt(Instant.now());

        categoryRepository.save(category);

        return CategoryResponse.from(category);
    }

    private Category resolveParent(User user, Category category, UUID parentId) {
        if (parentId == null) {
            return null;
        }

        if (parentId.equals(category.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A category cannot be its own parent");
        }

        if (categoryRepository.existsByParentId(category.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "A category with subcategories cannot become a subcategory");
        }

        Category parent = categoryRepository.findByIdAndUserId(parentId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent category not found"));

        if (parent.getParent() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A subcategory cannot have subcategories");
        }

        return parent;
    }
}
