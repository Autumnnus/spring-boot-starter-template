package com.autumnus.spring_boot_starter_template.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.messaging.notifications")
public record NotificationMessagingProperties(
        String exchange,
        String queue,
        String routingKey
) {
}
