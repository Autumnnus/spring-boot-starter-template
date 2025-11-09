package com.autumnus.spring_boot_starter_template.common.messaging.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationMessage {

    Long userId;
    String title;
    String message;
    String type;
}
