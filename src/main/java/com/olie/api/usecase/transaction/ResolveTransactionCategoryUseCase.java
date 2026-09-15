package com.olie.api.usecase.transaction;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.Category;
import com.olie.api.entity.User;
import com.olie.api.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveTransactionCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public Category execute(User user, UUID categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }
}
