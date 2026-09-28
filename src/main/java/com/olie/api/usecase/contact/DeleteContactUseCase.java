package com.olie.api.usecase.contact;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.Contact;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteContactUseCase {

    private final ContactRepository contactRepository;

    @Transactional
    public void execute(User user, UUID contactId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));

        contactRepository.delete(contact);
    }
}
