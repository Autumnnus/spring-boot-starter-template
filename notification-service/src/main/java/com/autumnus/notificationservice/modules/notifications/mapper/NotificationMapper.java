package com.autumnus.notificationservice.modules.notifications.mapper;

import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

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
