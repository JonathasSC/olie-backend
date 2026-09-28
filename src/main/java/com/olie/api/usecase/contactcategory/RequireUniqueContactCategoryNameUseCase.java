package com.olie.api.usecase.contactcategory;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.repository.ContactCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RequireUniqueContactCategoryNameUseCase {

    private final ContactCategoryRepository contactCategoryRepository;

    public void execute(User user, String name, UUID currentId) {
        contactCategoryRepository.findByUserIdAndNameIgnoreCase(user.getId(), name)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_CATEGORY",
                            "Já existe uma categoria chamada \"%s\".".formatted(existing.getName()));
                });
    }
}
