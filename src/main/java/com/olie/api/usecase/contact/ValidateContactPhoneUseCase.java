package com.olie.api.usecase.contact;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.olie.api.entity.User;
import com.olie.api.exception.ApiException;
import com.olie.api.repository.ContactRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateContactPhoneUseCase {

    private final ContactRepository contactRepository;

    /** Normaliza para E.164 e garante que nenhum outro contato do usuário usa o mesmo número. */
    public String execute(User user, String rawPhone, UUID currentContactId) {
        String phone = PhoneNumbers.normalize(rawPhone).orElseThrow(() -> new ApiException(
                HttpStatus.BAD_REQUEST, "INVALID_PHONE",
                "Telefone inválido: use o formato internacional com código do país, ex.: +5511999999999."));

        contactRepository.findByUserIdAndPhone(user.getId(), phone)
                .filter(existing -> !existing.getId().equals(currentContactId))
                .ifPresent(existing -> {
                    throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE_PHONE",
                            "O telefone %s já pertence ao contato \"%s\".".formatted(phone, existing.getName()));
                });

        return phone;
    }
}
