package com.autumnus.notificationservice.modules.notifications.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationMessage {
    Long userId;
    
    // Deprecated fields (for backward compatibility)
    @Deprecated
    String title;
    @Deprecated
    String message;
    
    // New i18n fields
    String recipientLanguage;  // Recipient's language preference
    String titleKey;           // i18n message key (e.g., "notification.welcome.title")
    String messageKey;         // i18n message key (e.g., "notification.welcome.message")
    Object[] messageArgs;      // Message parameters (e.g., [username])
    
    NotificationType type;

    public enum NotificationType {
        SUCCESS, INFO, WARNING, ERROR
    }
}
