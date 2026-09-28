package com.olie.api.dto.contact;

import java.util.UUID;

import com.olie.api.entity.ContactCategory;

public record ContactCategoryResponse(UUID id, String name, long contactsCount) {

    public static ContactCategoryResponse from(ContactCategory category, long contactsCount) {
        return new ContactCategoryResponse(category.getId(), category.getName(), contactsCount);
    }
}
