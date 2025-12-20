package com.autumnus.notificationservice.modules.notifications.mapper;

import com.autumnus.notificationservice.common.i18n.MessageService;
import com.autumnus.notificationservice.modules.notifications.dto.NotificationMessage;
import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final MessageService messageService;

    public Notification toEntity(NotificationMessage message) {
        if (message == null) {
            return null;
        }
        final Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        
        // Use i18n keys if provided, otherwise fall back to deprecated direct text
        if (message.getTitleKey() != null && message.getRecipientLanguage() != null) {
            String translatedTitle = messageService.getMessage(
                message.getTitleKey(),
                null,
                message.getRecipientLanguage()
            );
            notification.setTitle(translatedTitle);
        } else {
            // Backward compatibility: use direct title
            notification.setTitle(message.getTitle());
        }
        
        if (message.getMessageKey() != null && message.getRecipientLanguage() != null) {
            String translatedMessage = messageService.getMessage(
                message.getMessageKey(),
                message.getMessageArgs(),
                message.getRecipientLanguage()
            );
            notification.setMessage(translatedMessage);
        } else {
            // Backward compatibility: use direct message
            notification.setMessage(message.getMessage());
        }
        
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
