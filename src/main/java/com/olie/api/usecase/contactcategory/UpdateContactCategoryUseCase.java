package com.olie.api.usecase.contactcategory;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.contact.ContactCategoryRequest;
import com.olie.api.dto.contact.ContactCategoryResponse;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactCategoryRepository;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateContactCategoryUseCase {

    private final ContactCategoryRepository contactCategoryRepository;
    private final ContactRepository contactRepository;
    private final RequireUniqueContactCategoryNameUseCase requireUniqueContactCategoryNameUseCase;

    @Transactional
    public ContactCategoryResponse execute(User user, UUID categoryId, ContactCategoryRequest request) {
        ContactCategory category = contactCategoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact category not found"));

        String name = request.name().trim();
        requireUniqueContactCategoryNameUseCase.execute(user, name, categoryId);

        category.setName(name);
        category.setUpdatedAt(Instant.now());
        contactCategoryRepository.save(category);

        return ContactCategoryResponse.from(category, contactRepository.findAllByCategoryId(categoryId).size());
    }
}
