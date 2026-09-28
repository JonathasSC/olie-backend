package com.olie.api.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inquiry_recipients")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryRecipient {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false, updatable = false)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private InquiryRecipientStatus status = InquiryRecipientStatus.PENDING;

    /** Texto efetivamente enviado (a saudação depende do horário do envio). */
    private String messageText;

    /**
     * Quantas mensagens deste contato já foram entregues ao WhatsApp (texto = 1, depois uma por foto).
     * Retentativas e reenvios continuam daqui, sem repetir o que o contato já recebeu.
     */
    @Column(nullable = false)
    private int sentParts;

    @Column(nullable = false)
    private int attempts;

    private String failureReason;

    private Instant sentAt;
}
