package com.olie.api.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.olie.api.entity.Notification;
import com.olie.api.entity.PlannedItem;
import com.olie.api.entity.User;
import com.olie.api.repository.NotificationRepository;
import com.olie.api.repository.PlannedItemRepository;
import com.olie.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationWebSocketRelay {

    private static final String USER_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PlannedItemRepository plannedItemRepository;

    @KafkaListener(topics = NotificationTopics.PLANNED_ITEM_NOTIFICATIONS, groupId = "notification-websocket-relay")
    public void relay(NotificationEvent event) {
        userRepository.findById(event.userId()).ifPresent(user -> persist(event, user));
        messagingTemplate.convertAndSendToUser(event.userEmail(), USER_DESTINATION, event);
    }

    private void persist(NotificationEvent event, User user) {
        PlannedItem plannedItem = event.plannedItemId() != null
                ? plannedItemRepository.findById(event.plannedItemId()).orElse(null)
                : null;

        Notification notification = Notification.builder()
                .user(user)
                .type(event.type())
                .message(event.message())
                .plannedItem(plannedItem)
                .build();

        notificationRepository.save(notification);
    }
}
