package com.olie.api.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notifications")
public record NotificationProperties(int purchaseDateApproachingDays) {
}
