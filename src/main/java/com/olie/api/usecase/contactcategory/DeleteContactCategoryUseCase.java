package com.olie.api.usecase.contactcategory;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.entity.Contact;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.repository.ContactCategoryRepository;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteContactCategoryUseCase {

    private final ContactCategoryRepository contactCategoryRepository;
    private final ContactRepository contactRepository;

    /**
     * Remover uma categoria ainda em uso exige {@code unlinkContacts=true}: os contatos perdem só esse vínculo.
     * Contatos que ficariam sem nenhuma categoria bloqueiam a remoção (todo contato precisa de ao menos uma).
     */
    @Transactional
    public void execute(User user, UUID categoryId, boolean unlinkContacts) {
        ContactCategory category = contactCategoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact category not found"));
        List<Contact> contacts = contactRepository.findAllByCategoryId(categoryId);

        if (!contacts.isEmpty() && !unlinkContacts) {
            throw new ApiException(HttpStatus.CONFLICT, "CATEGORY_HAS_CONTACTS",
                    "A categoria tem %d contato(s). Confirme para desvinculá-los.".formatted(contacts.size()),
                    contacts.stream().map(Contact::getName).toList());
        }

        List<String> orphans = contacts.stream()
                .filter(contact -> contact.getCategories().size() == 1)
                .map(Contact::getName)
                .toList();
        if (!orphans.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "CONTACTS_WITHOUT_CATEGORY",
                    "Alguns contatos ficariam sem categoria. Mova-os para outra categoria antes de remover.",
                    orphans);
        }

        contacts.forEach(contact -> contact.getCategories().remove(category));
        contactRepository.saveAll(contacts);
        contactCategoryRepository.delete(category);
    }
}
