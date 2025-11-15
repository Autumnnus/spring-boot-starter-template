package com.autumnus.notificationservice.modules.notifications.dto;

import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationStatus;
import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        NotificationType type,
        NotificationStatus status,
        Instant createdAt
) {
}
