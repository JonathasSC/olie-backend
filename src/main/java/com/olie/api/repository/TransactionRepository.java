package com.olie.api.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.olie.api.entity.Transaction;
import com.olie.api.entity.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByUserId(UUID userId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(t.value), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumValueByUserIdAndType(@Param("userId") UUID userId, @Param("type") TransactionType type);

}
