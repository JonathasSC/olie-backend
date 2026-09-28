package com.olie.api.usecase.contact;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.olie.api.dto.contact.ContactFileFormat;
import com.olie.api.entity.ContactCategory;
import com.olie.api.entity.User;
import com.olie.api.repository.ContactCategoryRepository;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExportContactsUseCase {

    private final ContactRepository contactRepository;
    private final ContactCategoryRepository contactCategoryRepository;

    /** Gera o arquivo no mesmo formato aceito pela importação (serve também de backup). */
    @Transactional(readOnly = true)
    public byte[] execute(User user, ContactFileFormat format) {
        List<String> categories = contactCategoryRepository.findAllByUserIdOrderByNameAsc(user.getId()).stream()
                .map(ContactCategory::getName)
                .toList();

        List<Map<String, Object>> contacts = contactRepository.findAllByUserIdOrderByNameAsc(user.getId()).stream()
                .map(contact -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", contact.getName());
                    entry.put("phone", contact.getPhone());
                    entry.put("categories", contact.getCategories().stream().map(ContactCategory::getName).toList());
                    entry.put("active", contact.isActive());
                    entry.put("doNotContact", contact.isDoNotContact());
                    return entry;
                })
                .toList();

        Map<String, Object> document = new LinkedHashMap<>();
        document.put("categories", categories);
        document.put("contacts", contacts);

        return format.mapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(document);
    }
}
