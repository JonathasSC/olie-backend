package com.olie.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.olie.api.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findAllByUserIdOrderByNameAsc(UUID userId);

    Optional<Contact> findByIdAndUserId(UUID id, UUID userId);

    List<Contact> findAllByIdInAndUserId(Collection<UUID> ids, UUID userId);

    Optional<Contact> findByUserIdAndPhone(UUID userId, String phone);

    @Query("SELECT DISTINCT c FROM Contact c JOIN c.categories cat "
            + "WHERE c.user.id = :userId AND cat.id IN :categoryIds ORDER BY c.name")
    List<Contact> findAllByUserIdAndCategoryIds(
            @Param("userId") UUID userId, @Param("categoryIds") Collection<UUID> categoryIds);

    @Query("SELECT c FROM Contact c JOIN c.categories cat WHERE cat.id = :categoryId")
    List<Contact> findAllByCategoryId(@Param("categoryId") UUID categoryId);

}
