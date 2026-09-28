package com.olie.api.usecase.inquiry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.inquiry.InquiryDraftResponse;
import com.olie.api.dto.inquiry.InquiryDraftResponse.SkippedContact;
import com.olie.api.dto.inquiry.InquiryItemRequest;
import com.olie.api.entity.Contact;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetInquiryDraftUseCase {

    private final FindUserInquiryUseCase findUserInquiryUseCase;

    @Transactional(readOnly = true)
    public InquiryDraftResponse execute(User user, UUID inquiryId) {
        Inquiry inquiry = findUserInquiryUseCase.execute(user, inquiryId);

        List<UUID> contactIds = new ArrayList<>();
        List<SkippedContact> skipped = new ArrayList<>();
        for (InquiryRecipient recipient : inquiry.getRecipients()) {
            Contact contact = recipient.getContact();
            if (contact == null) {
                skipped.add(new SkippedContact(recipient.getContactName(), "Contato removido"));
            } else if (contact.isDoNotContact()) {
                skipped.add(new SkippedContact(contact.getName(), "Pediu para não ser contatado"));
            } else if (!contact.isActive()) {
                skipped.add(new SkippedContact(contact.getName(), "Contato inativo"));
            } else {
                contactIds.add(contact.getId());
            }
        }

        List<InquiryItemRequest> items = inquiry.getItems().stream()
                .map(item -> new InquiryItemRequest(item.getName(),
                        item.getPhoto() != null ? item.getPhoto().getId() : null))
                .toList();

        return new InquiryDraftResponse(
                new LinkedHashSet<>(inquiry.getCategories().stream().map(ContactCategory::getId).toList()),
                contactIds, items, skipped);
    }
}
