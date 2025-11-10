package com.autumnus.notificationservice.modules.notifications.listener;

import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.entity.Notification;
import com.autumnus.notificationservice.modules.notifications.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    public void publish(Notification notification) {
        if (notification.getUserId() == null) {
            log.warn("Skipping notification {} without user id", notification.getId());
            return;
        }
        final String destination = "/topic/notifications/" + notification.getUserId();
        final NotificationResponse payload = notificationMapper.toResponse(notification);
        messagingTemplate.convertAndSend(destination, payload);
        log.info("Sent notification {} to destination {}", notification.getId(), destination);
    }
}
