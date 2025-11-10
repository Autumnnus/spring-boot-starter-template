package com.autumnus.notificationservice.modules.notifications.service;

import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import com.autumnus.notificationservice.modules.notifications.messaging.NotificationMessage;

import java.util.List;

public interface NotificationService {

    Notification save(NotificationMessage message);

    List<NotificationResponse> getUserNotifications(Long userId);

    void markAsRead(Long id, Long userId);
}
