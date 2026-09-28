package com.olie.api.dto.contact;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.olie.api.entity.Contact;
import com.olie.api.entity.ContactCategory;

public record ContactResponse(
        UUID id,
        String name,
        String phone,
        List<UUID> categoryIds,
        boolean active,
        boolean doNotContact,
        boolean contactable,
        Instant createdAt) {

    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getPhone(),
                contact.getCategories().stream().map(ContactCategory::getId).toList(),
                contact.isActive(),
                contact.isDoNotContact(),
                contact.isContactable(),
                contact.getCreatedAt());
    }
}
