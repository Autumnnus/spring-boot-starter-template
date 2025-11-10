package com.autumnus.notificationservice.modules.notifications.repository;

import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countByIdAndUserIdAndStatus(Long id, Long userId, NotificationStatus status);
}
