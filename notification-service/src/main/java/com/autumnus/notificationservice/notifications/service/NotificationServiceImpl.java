package com.autumnus.notificationservice.notifications.service;

import com.autumnus.notificationservice.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.notifications.entity.Notification;
import com.autumnus.notificationservice.notifications.entity.NotificationStatus;
import com.autumnus.notificationservice.notifications.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification save(Notification notification) {
        log.info("Persisting notification for userId={} type={}", notification.getUserId(), notification.getType());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long userId, Long notificationId) {
        final Notification notification = notificationRepository.findById(notificationId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.READ);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
