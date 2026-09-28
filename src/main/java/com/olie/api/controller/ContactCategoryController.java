package com.olie.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.olie.api.dto.contact.ContactCategoryRequest;
import com.olie.api.dto.contact.ContactCategoryResponse;
import com.olie.api.entity.User;
import com.olie.api.usecase.contactcategory.CreateContactCategoryUseCase;
import com.olie.api.usecase.contactcategory.DeleteContactCategoryUseCase;
import com.olie.api.usecase.contactcategory.ListContactCategoriesUseCase;
import com.olie.api.usecase.contactcategory.UpdateContactCategoryUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/contact-categories")
@RequiredArgsConstructor
public class ContactCategoryController {

    private final ListContactCategoriesUseCase listContactCategoriesUseCase;
    private final CreateContactCategoryUseCase createContactCategoryUseCase;
    private final UpdateContactCategoryUseCase updateContactCategoryUseCase;
    private final DeleteContactCategoryUseCase deleteContactCategoryUseCase;

    @GetMapping
    public List<ContactCategoryResponse> list(@AuthenticationPrincipal User user) {
        return listContactCategoriesUseCase.execute(user);
    }

    @PostMapping
    public ResponseEntity<ContactCategoryResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody ContactCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createContactCategoryUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public ContactCategoryResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody ContactCategoryRequest request) {
        return updateContactCategoryUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean unlinkContacts) {
        deleteContactCategoryUseCase.execute(user, id, unlinkContacts);
        return ResponseEntity.noContent().build();
    }
}
