package com.autumnus.spring_boot_starter_template.common.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${application.rabbitmq.exchange:notifications.exchange}")
    private String exchange;

    @Value("${application.rabbitmq.routing-key:notifications.new}")
    private String routingKey;

    public void sendNotification(Long userId, String title, String message, NotificationType type) {
        NotificationMessage notificationMessage = new NotificationMessage(userId, title, message, type);
        sendNotification(notificationMessage);
    }

    public void sendNotification(NotificationMessage message) {
        try {
            log.info("Sending notification to user {}: {}", message.getUserId(), message.getTitle());
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.info("Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            // Don't throw exception - notification failure shouldn't break the main flow
        }
    }
}
