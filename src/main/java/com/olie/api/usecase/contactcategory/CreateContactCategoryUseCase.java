package com.olie.api.usecase.contactcategory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.contact.ContactCategoryRequest;
import com.olie.api.dto.contact.ContactCategoryResponse;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateContactCategoryUseCase {

    private final ContactCategoryRepository contactCategoryRepository;
    private final RequireUniqueContactCategoryNameUseCase requireUniqueContactCategoryNameUseCase;

    @Transactional
    public ContactCategoryResponse execute(User user, ContactCategoryRequest request) {
        String name = request.name().trim();
        requireUniqueContactCategoryNameUseCase.execute(user, name, null);

        ContactCategory category = ContactCategory.builder().user(user).name(name).build();
        contactCategoryRepository.save(category);

        return ContactCategoryResponse.from(category, 0);
    }
}
