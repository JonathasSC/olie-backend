package com.olie.api.dto.inquiry;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dados de uma consulta anterior no mesmo formato do {@code POST /inquiries}, para "repetir consulta".
 * Contatos removidos, inativos ou que pediram para não ser contatados vão para {@code skippedContacts}.
 */
public record InquiryDraftResponse(
        Set<UUID> categoryIds,
        List<UUID> contactIds,
        List<InquiryItemRequest> items,
        List<SkippedContact> skippedContacts) {

    public record SkippedContact(String contactName, String reason) {
    }
}
