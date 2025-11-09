package com.autumnus.notificationservice.listener;

import com.autumnus.notificationservice.dto.NotificationMessage;
import com.autumnus.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${application.rabbitmq.queue}")
    public void receiveNotification(NotificationMessage message) {
        log.info("Received notification message for user: {}", message.getUserId());
        try {
            notificationService.createNotification(message);
            log.info("Successfully processed notification for user: {}", message.getUserId());
        } catch (Exception e) {
            log.error("Failed to process notification message", e);
            throw e;
        }
    }
}
