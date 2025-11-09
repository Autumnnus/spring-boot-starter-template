package com.autumnus.notificationservice.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private Long userId;
    private String title;
    private String message;
    private String type;
}
