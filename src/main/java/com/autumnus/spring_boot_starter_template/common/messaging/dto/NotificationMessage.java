package com.autumnus.spring_boot_starter_template.common.messaging.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class NotificationMessage {
    Long userId;
    String title;
    String message;
    NotificationType type;

    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
}