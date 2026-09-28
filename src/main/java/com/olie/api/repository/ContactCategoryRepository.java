package com.olie.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.ContactCategory;

public interface ContactCategoryRepository extends JpaRepository<ContactCategory, UUID> {

    List<ContactCategory> findAllByUserIdOrderByNameAsc(UUID userId);

    Optional<ContactCategory> findByIdAndUserId(UUID id, UUID userId);

    List<ContactCategory> findAllByIdInAndUserId(Collection<UUID> ids, UUID userId);

    Optional<ContactCategory> findByUserIdAndNameIgnoreCase(UUID userId, String name);

}
