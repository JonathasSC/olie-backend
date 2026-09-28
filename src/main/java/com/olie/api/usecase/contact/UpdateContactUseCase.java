package com.olie.api.usecase.contact;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.contact.ContactRequest;
import com.olie.api.dto.contact.ContactResponse;
import com.olie.api.entity.Contact;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateContactUseCase {

    private final ContactRepository contactRepository;
    private final ValidateContactPhoneUseCase validateContactPhoneUseCase;
    private final ResolveContactCategoriesUseCase resolveContactCategoriesUseCase;

    @Transactional
    public ContactResponse execute(User user, UUID contactId, ContactRequest request) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));

        contact.setName(request.name().trim());
        contact.setPhone(validateContactPhoneUseCase.execute(user, request.phone(), contactId));
        if (request.active() != null) {
            contact.setActive(request.active());
        }
        if (request.doNotContact() != null) {
            contact.setDoNotContact(request.doNotContact());
        }
        contact.setCategories(resolveContactCategoriesUseCase.execute(user, request.categoryIds()));
        contact.setUpdatedAt(Instant.now());
        contactRepository.save(contact);

        return ContactResponse.from(contact);
    }
}
