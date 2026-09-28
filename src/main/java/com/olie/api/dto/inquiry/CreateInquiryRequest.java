package com.olie.api.dto.inquiry;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateInquiryRequest(
        Set<UUID> categoryIds,
        @NotEmpty List<UUID> contactIds,
        @NotEmpty List<@Valid InquiryItemRequest> items,
        Boolean simulate,
        Boolean confirmDuplicates) {

    /** Campos opcionais: ausentes valem {@code false} (o Jackson 3 rejeita {@code null} em primitivos). */
    public boolean isSimulation() {
        return Boolean.TRUE.equals(simulate);
    }

    public boolean duplicatesConfirmed() {
        return Boolean.TRUE.equals(confirmDuplicates);
    }
}
