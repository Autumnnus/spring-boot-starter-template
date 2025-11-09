package com.autumnus.notificationservice.service;

import com.autumnus.notificationservice.dto.NotificationMessage;
import com.autumnus.notificationservice.dto.NotificationResponse;
import com.autumnus.notificationservice.entity.Notification;
import com.autumnus.notificationservice.entity.NotificationStatus;
import com.autumnus.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationResponse createNotification(NotificationMessage message) {
        log.info("Creating notification for user: {}, type: {}", message.getUserId(), message.getType());

        Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        notification.setTitle(message.getTitle());
        notification.setMessage(message.getMessage());
        notification.setType(message.getType());
        notification.setStatus(NotificationStatus.UNREAD);

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created with id: {}", saved.getId());

        NotificationResponse response = toResponse(saved);

        // Send real-time notification via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    message.getUserId().toString(),
                    "/queue/notifications",
                    response
            );
            log.info("WebSocket notification sent to user: {}", message.getUserId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification", e);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, NotificationStatus status, Pageable pageable) {
        log.debug("Fetching notifications for user: {}, status: {}", userId, status);

        Page<Notification> notifications;
        if (status != null) {
            notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return notifications.map(this::toResponse);
    }

    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read for user {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to user");
        }

        if (notification.getStatus() == NotificationStatus.UNREAD) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(Instant.now());
            notification = notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        Page<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.UNREAD, Pageable.unpaged());

        Instant now = Instant.now();
        unreadNotifications.forEach(notification -> {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(now);
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
