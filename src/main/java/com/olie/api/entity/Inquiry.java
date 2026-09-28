package com.olie.api.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inquiries")
// o disparador e as ações do usuário (pausar/cancelar) alteram colunas diferentes em paralelo
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InquiryStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private InquiryPauseReason pauseReason;

    /** Simulação: percorre todo o fluxo sem enviar nada ao WhatsApp. */
    @Column(nullable = false)
    private boolean simulated;

    /** Quando o próximo contato será processado (base da contagem regressiva na tela). */
    private Instant nextSendAt;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant finishedAt;

    @ManyToMany
    @JoinTable(
            name = "inquiry_categories",
            joinColumns = @JoinColumn(name = "inquiry_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @BatchSize(size = 50)
    @Builder.Default
    private Set<ContactCategory> categories = new LinkedHashSet<>();

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @BatchSize(size = 50)
    @Builder.Default
    private List<InquiryItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position")
    @BatchSize(size = 50)
    @Builder.Default
    private List<InquiryRecipient> recipients = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    public void addItem(InquiryItem item) {
        item.setInquiry(this);
        item.setPosition(items.size());
        items.add(item);
    }

    public void addRecipient(InquiryRecipient recipient) {
        recipient.setInquiry(this);
        recipient.setPosition(recipients.size());
        recipients.add(recipient);
    }
}
