package com.olie.api.usecase.category;

import java.util.List;

import org.springframework.stereotype.Service;

import com.olie.api.dto.category.CategoryResponse;
import com.olie.api.entity.User;
import com.olie.api.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> execute(User user) {
        return categoryRepository.findAllByUserId(user.getId()).stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
