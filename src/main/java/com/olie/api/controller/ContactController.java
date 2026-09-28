package com.olie.api.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.olie.api.dto.contact.ContactFileFormat;
import com.olie.api.dto.contact.ContactImportResponse;
import com.olie.api.dto.contact.ContactRequest;
import com.olie.api.dto.contact.ContactResponse;
import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.usecase.contact.CreateContactUseCase;
import com.olie.api.usecase.contact.DeleteContactUseCase;
import com.olie.api.usecase.contact.ExportContactsUseCase;
import com.olie.api.usecase.contact.ImportContactsUseCase;
import com.olie.api.usecase.contact.ListContactsUseCase;
import com.olie.api.usecase.contact.UpdateContactUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(ApiV1.PREFIX + "/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ListContactsUseCase listContactsUseCase;
    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final DeleteContactUseCase deleteContactUseCase;
    private final ImportContactsUseCase importContactsUseCase;
    private final ExportContactsUseCase exportContactsUseCase;

    @GetMapping
    public List<ContactResponse> list(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Set<UUID> categoryId,
            @RequestParam(required = false) Boolean contactable) {
        return listContactsUseCase.execute(user, categoryId, contactable);
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(
            @AuthenticationPrincipal User user, @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createContactUseCase.execute(user, request));
    }

    @PutMapping("/{id}")
    public ContactResponse update(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody ContactRequest request) {
        return updateContactUseCase.execute(user, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        deleteContactUseCase.execute(user, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ContactImportResponse importFile(
            @AuthenticationPrincipal User user, @RequestPart("file") MultipartFile file) throws IOException {
        ContactFileFormat format = ContactFileFormat.fromFilename(file.getOriginalFilename())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FORMAT",
                        "Use um arquivo .yaml, .yml ou .json."));
        return importContactsUseCase.execute(user, file.getBytes(), format);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @AuthenticationPrincipal User user, @RequestParam(defaultValue = "YAML") ContactFileFormat format) {
        return ResponseEntity.ok()
                .contentType(format.mediaType())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("contatos." + format.extension())
                        .build()
                        .toString())
                .body(exportContactsUseCase.execute(user, format));
    }
}
