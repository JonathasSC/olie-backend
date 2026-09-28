package com.olie.api.usecase.contact;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.contact.ContactRequest;
import com.olie.api.dto.contact.ContactResponse;
import com.olie.api.entity.Contact;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateContactUseCase {

    private final ContactRepository contactRepository;
    private final ValidateContactPhoneUseCase validateContactPhoneUseCase;
    private final ResolveContactCategoriesUseCase resolveContactCategoriesUseCase;

    @Transactional
    public ContactResponse execute(User user, ContactRequest request) {
        String phone = validateContactPhoneUseCase.execute(user, request.phone(), null);

        Contact contact = Contact.builder()
                .user(user)
                .name(request.name().trim())
                .phone(phone)
                .active(request.active() == null || request.active())
                .doNotContact(Boolean.TRUE.equals(request.doNotContact()))
                .categories(resolveContactCategoriesUseCase.execute(user, request.categoryIds()))
                .build();
        contactRepository.save(contact);

        return ContactResponse.from(contact);
    }
}
