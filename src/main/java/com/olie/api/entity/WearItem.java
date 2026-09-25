package com.olie.api.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wear_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WearItem {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private int expectedLifespan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WearItemLifespanUnit expectedLifespanUnit;

    @OneToMany(mappedBy = "wearItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WearItemCycle> cycles = new ArrayList<>();

    @Column(name = "replacement_alert_sent", nullable = false)
    @Builder.Default
    private boolean replacementAlertSent = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public void addCycle(WearItemCycle cycle) {
        cycle.setWearItem(this);
        cycles.add(cycle);
    }

    public WearItemCycle getCurrentCycle() {
        return cycles.stream()
                .filter(WearItemCycle::isCurrent)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Wear item " + id + " has no current cycle"));
    }

    /** Ciclos já encerrados, do mais recente para o mais antigo. */
    public List<WearItemCycle> getClosedCycles() {
        return cycles.stream()
                .filter(cycle -> !cycle.isCurrent())
                .sorted(Comparator.comparing(WearItemCycle::getRemovalDate).reversed())
                .toList();
    }
}
