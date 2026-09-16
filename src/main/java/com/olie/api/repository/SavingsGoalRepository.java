package com.olie.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.olie.api.entity.SavingsGoal;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

    List<SavingsGoal> findAllByUserId(UUID userId);

    Optional<SavingsGoal> findByIdAndUserId(UUID id, UUID userId);

}
