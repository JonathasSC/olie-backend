package com.olie.api.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.olie.api.entity.InquiryPhoto;

public interface InquiryPhotoRepository extends JpaRepository<InquiryPhoto, UUID> {

    Optional<InquiryPhoto> findByIdAndUserId(UUID id, UUID userId);

    /** Fotos enviadas mas nunca usadas em uma consulta (upload abandonado). */
    @Query("SELECT p FROM InquiryPhoto p WHERE p.createdAt < :before "
            + "AND NOT EXISTS (SELECT 1 FROM InquiryItem i WHERE i.photo = p)")
    List<InquiryPhoto> findAllUnusedCreatedBefore(@Param("before") Instant before);

}
