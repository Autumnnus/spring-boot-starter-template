package com.autumnus.notificationservice.modules.notifications.entity;

import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationStatus;
import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2048)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private NotificationStatus status = NotificationStatus.UNREAD;

    public enum NotificationType {
        INFO,
        WARNING,
        SUCCESS,
        ERROR
    }

    public enum NotificationStatus {
        READ,
        UNREAD
    }
}
