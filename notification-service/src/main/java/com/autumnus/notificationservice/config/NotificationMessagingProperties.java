package com.autumnus.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.messaging.notifications")
public record NotificationMessagingProperties(
        String exchange,
        String queue,
        String routingKey
) {
}
