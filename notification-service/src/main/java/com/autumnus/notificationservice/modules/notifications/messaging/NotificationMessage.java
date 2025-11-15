package com.autumnus.notificationservice.modules.notifications.messaging;

public record NotificationMessage(
        Long userId,
        String title,
        String message,
        NotificationType type
) {
    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
}
