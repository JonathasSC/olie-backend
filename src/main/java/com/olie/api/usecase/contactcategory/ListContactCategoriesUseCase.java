package com.olie.api.usecase.contactcategory;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.contact.ContactCategoryResponse;
import com.olie.api.entity.Contact;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactCategoryRepository;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListContactCategoriesUseCase {

    private final ContactCategoryRepository contactCategoryRepository;
    private final ContactRepository contactRepository;

    @Transactional(readOnly = true)
    public List<ContactCategoryResponse> execute(User user) {
        List<Contact> contacts = contactRepository.findAllByUserIdOrderByNameAsc(user.getId());

        return contactCategoryRepository.findAllByUserIdOrderByNameAsc(user.getId()).stream()
                .map(category -> ContactCategoryResponse.from(category, contacts.stream()
                        .filter(contact -> contact.getCategories().contains(category))
                        .count()))
                .toList();
    }
}
