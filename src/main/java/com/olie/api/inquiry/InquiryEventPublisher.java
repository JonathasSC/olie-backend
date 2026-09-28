package com.olie.api.inquiry;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.olie.api.dto.inquiry.InquiryEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InquiryEventPublisher {

    public static final String USER_DESTINATION = "/queue/inquiries";

    private final SimpMessagingTemplate messagingTemplate;

    /** Dentro de uma transação, só envia depois do commit — a tela nunca mostra um estado que foi desfeito. */
    public void publish(String userEmail, InquiryEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(userEmail, event);
                }
            });
        } else {
            send(userEmail, event);
        }
    }

    private void send(String userEmail, InquiryEvent event) {
        messagingTemplate.convertAndSendToUser(userEmail, USER_DESTINATION, event);
    }
}
