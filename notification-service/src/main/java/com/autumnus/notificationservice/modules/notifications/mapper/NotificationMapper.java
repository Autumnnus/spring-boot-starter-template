package com.autumnus.notificationservice.modules.notifications.mapper;

import com.autumnus.notificationservice.modules.notifications.dto.NotificationMessage;
import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationMessage message) {
        if (message == null) {
            return null;
        }
        final Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        notification.setTitle(message.getTitle());
        notification.setMessage(message.getMessage());
        if (message.getType() != null) {
            notification.setType(Notification.NotificationType.valueOf(message.getType().name()));
        }
        return notification;
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getStatus(),
                notification.getCreatedAt()
        );
    }
}
