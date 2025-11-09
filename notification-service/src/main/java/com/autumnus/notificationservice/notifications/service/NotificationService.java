package com.autumnus.notificationservice.notifications.service;

import com.autumnus.notificationservice.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.notifications.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification save(Notification notification);

    List<NotificationResponse> getUserNotifications(Long userId);

    NotificationResponse markAsRead(Long userId, Long notificationId);
}
