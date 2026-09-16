package com.olie.api.notification;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(NotificationEvent event) {
        kafkaTemplate.send(NotificationTopics.PLANNED_ITEM_NOTIFICATIONS, event.userId().toString(), event);
    }
}
