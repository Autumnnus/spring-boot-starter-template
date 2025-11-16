package com.autumnus.notificationservice.modules.notifications.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationMessage {
    Long userId;
    String title;
    String message;
    NotificationType type;

    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
}
