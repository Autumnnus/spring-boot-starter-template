package com.autumnus.notificationservice.modules.notifications.messaging;

import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationType;

public record NotificationMessage(
        Long userId,
        String title,
        String message,
        NotificationType type
) {
}
