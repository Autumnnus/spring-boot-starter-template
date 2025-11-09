package com.autumnus.notificationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.websocket")
public record WebSocketProperties(
        String destination
) {
}
