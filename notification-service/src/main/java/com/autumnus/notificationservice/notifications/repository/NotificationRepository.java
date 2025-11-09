package com.autumnus.notificationservice.notifications.repository;

import com.autumnus.notificationservice.notifications.entity.Notification;
import com.autumnus.notificationservice.notifications.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);
}
