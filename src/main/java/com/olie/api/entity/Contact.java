package com.olie.api.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    /** Telefone no formato E.164 (ex.: +5511999999999). */
    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /** Contato pediu para não ser mais consultado: fica fora de qualquer envio, mesmo ativo. */
    @Column(name = "do_not_contact", nullable = false)
    @Builder.Default
    private boolean doNotContact = false;

    @ManyToMany
    @JoinTable(
            name = "contact_category_assignments",
            joinColumns = @JoinColumn(name = "contact_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    @BatchSize(size = 50)
    @Builder.Default
    private Set<ContactCategory> categories = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public boolean isContactable() {
        return active && !doNotContact;
    }
}
