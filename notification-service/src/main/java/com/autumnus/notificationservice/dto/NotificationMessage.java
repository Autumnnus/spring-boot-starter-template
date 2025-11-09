package com.autumnus.notificationservice.dto;

import com.autumnus.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
}
