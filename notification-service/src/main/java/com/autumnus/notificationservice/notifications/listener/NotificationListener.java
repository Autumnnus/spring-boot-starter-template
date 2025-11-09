package com.autumnus.notificationservice.notifications.listener;

import com.autumnus.notificationservice.config.NotificationMessagingProperties;
import com.autumnus.notificationservice.config.WebSocketProperties;
import com.autumnus.notificationservice.notifications.dto.NotificationMessage;
import com.autumnus.notificationservice.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.notifications.entity.Notification;
import com.autumnus.notificationservice.notifications.entity.NotificationStatus;
import com.autumnus.notificationservice.notifications.entity.NotificationType;
import com.autumnus.notificationservice.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final NotificationMessagingProperties messagingProperties;
    private final WebSocketProperties webSocketProperties;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "#{notificationsQueue.name}")
    public void onNotification(NotificationMessage message) {
        if (message.getUserId() == null) {
            log.warn("Received notification without userId. Discarding message: {}", message);
            return;
        }
        final NotificationType type;
        try {
            type = NotificationType.valueOf(message.getType());
        } catch (IllegalArgumentException | NullPointerException ex) {
            log.warn("Unknown notification type '{}' - defaulting to INFO", message.getType());
            type = NotificationType.INFO;
        }
        log.info("Received notification from exchange={} queue={} for userId={} type={}",
                messagingProperties.exchange(), messagingProperties.queue(), message.getUserId(), type);
        final Notification notification = new Notification();
        notification.setUserId(message.getUserId());
        notification.setTitle(message.getTitle());
        notification.setMessage(message.getMessage());
        notification.setType(type);
        notification.setStatus(NotificationStatus.UNREAD);
        final Notification saved = notificationService.save(notification);
        final NotificationResponse response = NotificationResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .type(saved.getType())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
        final String destination = "%s/%s".formatted(webSocketProperties.destination(), saved.getUserId());
        log.info("Broadcasting notification id={} to destination={}", saved.getId(), destination);
        messagingTemplate.convertAndSend(destination, response);
    }
}
