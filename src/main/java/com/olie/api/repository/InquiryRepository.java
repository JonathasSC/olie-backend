package com.olie.api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.olie.api.entity.Inquiry;
import com.olie.api.entity.InquiryStatus;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {

    Optional<Inquiry> findByIdAndUserId(UUID id, UUID userId);

    List<Inquiry> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT q.id FROM Inquiry q WHERE q.status = :status AND q.nextSendAt <= :now ORDER BY q.nextSendAt")
    List<UUID> findIdsDue(@Param("status") InquiryStatus status, @Param("now") Instant now);

}
