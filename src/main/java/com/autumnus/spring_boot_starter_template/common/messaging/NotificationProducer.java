package com.autumnus.spring_boot_starter_template.common.messaging;

import com.autumnus.spring_boot_starter_template.common.config.NotificationMessagingProperties;
import com.autumnus.spring_boot_starter_template.common.messaging.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationMessagingProperties properties;

    public void sendNotification(NotificationMessage message) {
        log.info("Publishing notification message to exchange={} routingKey={} userId={}",
                properties.exchange(), properties.routingKey(), message.getUserId());
        try {
            rabbitTemplate.convertAndSend(properties.exchange(), properties.routingKey(), message);
        } catch (AmqpException exception) {
            log.error("Failed to publish notification for userId={}", message.getUserId(), exception);
        }
    }
}
