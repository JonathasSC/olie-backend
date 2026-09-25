package com.olie.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.WearItem;

public interface WearItemRepository extends JpaRepository<WearItem, UUID> {

    List<WearItem> findAllByUserId(UUID userId);

    Optional<WearItem> findByIdAndUserId(UUID id, UUID userId);

    List<WearItem> findAllByReplacementAlertSentFalse();

}
