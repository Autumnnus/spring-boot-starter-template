package com.autumnus.notificationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@org.springframework.stereotype.Component
@ConfigurationProperties(prefix = "application.messaging.notifications")
public class NotificationMessagingProperties {

    private String exchange = "notifications.exchange";
    private String queue = "notifications.queue";
    private String routingKey = "notifications.key";
}
