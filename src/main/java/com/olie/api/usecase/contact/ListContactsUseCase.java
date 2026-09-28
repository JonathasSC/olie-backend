package com.olie.api.usecase.contact;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.contact.ContactResponse;
import com.olie.api.entity.Contact;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListContactsUseCase {

    private final ContactRepository contactRepository;

    @Transactional(readOnly = true)
    public List<ContactResponse> execute(User user, Set<UUID> categoryIds, Boolean contactable) {
        List<Contact> contacts = categoryIds == null || categoryIds.isEmpty()
                ? contactRepository.findAllByUserIdOrderByNameAsc(user.getId())
                : contactRepository.findAllByUserIdAndCategoryIds(user.getId(), categoryIds);

        return contacts.stream()
                .filter(contact -> contactable == null || contact.isContactable() == contactable)
                .map(ContactResponse::from)
                .toList();
    }
}
