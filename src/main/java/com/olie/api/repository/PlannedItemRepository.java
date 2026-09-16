package com.olie.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.PlannedItem;

public interface PlannedItemRepository extends JpaRepository<PlannedItem, UUID> {

    List<PlannedItem> findAllByUserId(UUID userId);

    Optional<PlannedItem> findByIdAndUserId(UUID id, UUID userId);

    List<PlannedItem> findAllByDateAlertSentFalseAndEstimatedDateLessThanEqual(LocalDate date);

    List<PlannedItem> findAllByBalanceAlertSentFalseAndCategoryIsNotNull();

}
