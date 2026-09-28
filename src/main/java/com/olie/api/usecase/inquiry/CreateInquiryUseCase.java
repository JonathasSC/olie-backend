package com.olie.api.usecase.inquiry;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.olie.api.dto.inquiry.CreateInquiryRequest;
import com.olie.api.dto.inquiry.DuplicateInquiryWarning;
import com.olie.api.dto.inquiry.InquiryItemRequest;
import com.olie.api.dto.inquiry.InquiryResponse;
import com.olie.api.entity.Contact;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryItem;
import com.olie.api.entity.InquiryPhoto;
import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryRecipientStatus;
import com.olie.api.entity.InquiryStatus;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.inquiry.InquiryProperties;
import com.olie.api.repository.ContactCategoryRepository;
import com.olie.api.repository.ContactRepository;
import com.olie.api.repository.InquiryPhotoRepository;
import com.olie.api.repository.InquiryRecipientRepository;
import com.olie.api.repository.InquiryRepository;
import com.olie.api.whatsapp.WhatsAppProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateInquiryUseCase {

    private final InquiryRepository inquiryRepository;
    private final InquiryRecipientRepository inquiryRecipientRepository;
    private final InquiryPhotoRepository inquiryPhotoRepository;
    private final ContactRepository contactRepository;
    private final ContactCategoryRepository contactCategoryRepository;
    private final WhatsAppProvider whatsAppProvider;
    private final InquiryProperties properties;
    private final Clock clock;

    /**
     * Valida e enfileira a consulta; o envio começa em até um segundo, feito pelo disparador. A confirmação com
     * o número de destinatários é do frontend — este endpoint já é a confirmação.
     */
    @Transactional
    public InquiryResponse execute(User user, CreateInquiryRequest request) {
        if (request.items().size() > properties.maxItems()) {
            throw PreviewInquiryUseCase.tooManyItems(properties.maxItems());
        }

        List<Contact> contacts = resolveContacts(user, request.contactIds());
        if (contacts.size() > properties.maxRecipients()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TOO_MANY_RECIPIENTS",
                    "Uma consulta pode ter no máximo %d contatos.".formatted(properties.maxRecipients()));
        }

        Set<ContactCategory> categories = resolveCategories(user, request.categoryIds());
        List<InquiryItem> items = request.items().stream().map(item -> toItem(user, item)).toList();

        if (!request.isSimulation()) {
            if (!whatsAppProvider.connection(user.getId()).isConnected()) {
                throw new ApiException(HttpStatus.CONFLICT, "WHATSAPP_NOT_CONNECTED",
                        "Conecte o WhatsApp antes de enviar (ou use a simulação).");
            }
            if (!request.duplicatesConfirmed()) {
                List<DuplicateInquiryWarning> duplicates = findDuplicates(user, contacts, request.items());
                if (!duplicates.isEmpty()) {
                    throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_INQUIRY",
                            "Alguns contatos já receberam estes itens recentemente. Confirme para enviar mesmo assim.",
                            duplicates);
                }
            }
        }

        Instant now = Instant.now(clock);
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .status(InquiryStatus.SENDING)
                .simulated(request.isSimulation())
                .startedAt(now)
                .nextSendAt(now)
                .categories(categories)
                .build();
        items.forEach(inquiry::addItem);
        contacts.forEach(contact -> inquiry.addRecipient(InquiryRecipient.builder()
                .contact(contact)
                .contactName(contact.getName())
                .phone(contact.getPhone())
                .status(InquiryRecipientStatus.PENDING)
                .build()));

        inquiryRepository.save(inquiry);

        return InquiryResponse.from(inquiry);
    }

    private List<Contact> resolveContacts(User user, List<UUID> contactIds) {
        Set<UUID> ordered = new LinkedHashSet<>(contactIds);
        Map<UUID, Contact> found = contactRepository.findAllByIdInAndUserId(ordered, user.getId()).stream()
                .collect(Collectors.toMap(Contact::getId, Function.identity()));
        if (found.size() != ordered.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
        }

        List<Contact> contacts = ordered.stream().map(found::get).toList();
        List<String> blocked = contacts.stream()
                .filter(contact -> !contact.isContactable())
                .map(Contact::getName)
                .toList();
        if (!blocked.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CONTACTS_NOT_CONTACTABLE",
                    "Contatos inativos ou que pediram para não ser contatados não podem receber consultas.", blocked);
        }
        return contacts;
    }

    private Set<ContactCategory> resolveCategories(User user, Set<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<ContactCategory> categories = contactCategoryRepository.findAllByIdInAndUserId(categoryIds, user.getId());
        if (categories.size() != categoryIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact category not found");
        }
        return new LinkedHashSet<>(categories);
    }

    private InquiryItem toItem(User user, InquiryItemRequest request) {
        InquiryPhoto photo = request.photoId() == null ? null
                : inquiryPhotoRepository.findByIdAndUserId(request.photoId(), user.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));

        return InquiryItem.builder().name(request.name().trim()).photo(photo).build();
    }

    private List<DuplicateInquiryWarning> findDuplicates(
            User user, List<Contact> contacts, List<InquiryItemRequest> items) {
        Set<String> itemNames = items.stream()
                .map(item -> item.name().trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Instant since = Instant.now(clock).minus(properties.duplicateWindow());

        return inquiryRecipientRepository.findRecentlySent(user.getId(),
                contacts.stream().map(Contact::getId).toList(), itemNames, since, InquiryRecipientStatus.SENT);
    }
}
