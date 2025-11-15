package com.autumnus.notificationservice.modules.notifications.service;

import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import com.autumnus.notificationservice.modules.notifications.entity.Notification.NotificationStatus;
import com.autumnus.notificationservice.modules.notifications.listener.NotificationWebSocketPublisher;
import com.autumnus.notificationservice.modules.notifications.mapper.NotificationMapper;
import com.autumnus.notificationservice.modules.notifications.messaging.NotificationMessage;
import com.autumnus.notificationservice.modules.notifications.repository.NotificationRepository;
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
    private final NotificationMapper notificationMapper;
    private final NotificationWebSocketPublisher webSocketPublisher;

    @Override
    public Notification save(NotificationMessage message) {
        System.out.println("save message: " + message);
        final Notification notification = new Notification();
        notification.setUserId(message.userId());
        notification.setTitle(message.title());
        notification.setMessage(message.message());
        notification.setType(message.type());
        final Notification saved = notificationRepository.save(notification);
        System.out.println("Persisted notification {} for user {}" + saved);
        log.info("Persisted notification {} for user {}", saved.getId(), saved.getUserId());
        webSocketPublisher.publish(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        System.out.println("Get User Notifications User ID: " + userId);
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        System.out.println("Mark as Read User ID: " + userId);
        final Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Notification not found");
        }
        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

}
