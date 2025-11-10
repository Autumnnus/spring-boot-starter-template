package com.autumnus.spring_boot_starter_template.common.messaging.dto;

import com.autumnus.spring_boot_starter_template.common.messaging.dto.NotificationMessage.NotificationType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationMessage {

    Long userId;
    String title;
    String message;
    NotificationType type;

    public enum NotificationType {
        INFO,
        WARNING,
        SUCCESS,
        ERROR
    }
}
