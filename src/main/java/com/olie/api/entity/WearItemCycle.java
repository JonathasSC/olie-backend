package com.olie.api.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wear_item_cycles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WearItemCycle {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wear_item_id", nullable = false, updatable = false)
    private WearItem wearItem;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    private LocalDate installationDate;

    private LocalDate removalDate;

    private BigDecimal purchaseValue;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public boolean isCurrent() {
        return removalDate == null;
    }
}
