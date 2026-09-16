package com.olie.api.notification;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class NotificationKafkaConfig {

    @Bean
    public NewTopic plannedItemNotificationsTopic() {
        return TopicBuilder.name(NotificationTopics.PLANNED_ITEM_NOTIFICATIONS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
