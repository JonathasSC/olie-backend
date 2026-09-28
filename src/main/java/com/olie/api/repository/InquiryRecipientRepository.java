package com.olie.api.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.olie.api.dto.inquiry.DuplicateInquiryWarning;
import com.olie.api.entity.InquiryRecipient;
import com.olie.api.entity.InquiryRecipientStatus;

public interface InquiryRecipientRepository extends JpaRepository<InquiryRecipient, UUID> {

    List<InquiryRecipient> findAllByStatus(InquiryRecipientStatus status);

    /** Itens já enviados de verdade (não simulados) para estes contatos desde {@code since}. */
    @Query("SELECT new com.olie.api.dto.inquiry.DuplicateInquiryWarning(c.id, r.contactName, i.name, r.sentAt) "
            + "FROM InquiryRecipient r JOIN r.contact c JOIN r.inquiry q JOIN q.items i "
            + "WHERE q.user.id = :userId AND q.simulated = false AND r.status = :sent AND r.sentAt >= :since "
            + "AND c.id IN :contactIds AND LOWER(TRIM(i.name)) IN :itemNames "
            + "ORDER BY r.sentAt DESC")
    List<DuplicateInquiryWarning> findRecentlySent(
            @Param("userId") UUID userId,
            @Param("contactIds") Collection<UUID> contactIds,
            @Param("itemNames") Collection<String> itemNames,
            @Param("since") Instant since,
            @Param("sent") InquiryRecipientStatus sent);

}
