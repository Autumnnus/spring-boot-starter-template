package com.autumnus.notificationservice.notifications.dto;

import com.autumnus.notificationservice.notifications.entity.NotificationStatus;
import com.autumnus.notificationservice.notifications.entity.NotificationType;
import lombok.Builder;

import java.time.Instant;

@Builder
public record NotificationResponse(
        Long id,
        String title,
        String message,
        NotificationType type,
        NotificationStatus status,
        Instant createdAt
) {
}
