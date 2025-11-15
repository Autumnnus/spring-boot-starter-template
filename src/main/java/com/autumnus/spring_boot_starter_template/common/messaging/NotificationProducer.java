package com.autumnus.spring_boot_starter_template.common.messaging;

import com.autumnus.spring_boot_starter_template.common.config.NotificationMessagingProperties;
import com.autumnus.spring_boot_starter_template.common.messaging.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationMessagingProperties properties;

    public void send(NotificationMessage message) {
        if (message == null) {
            log.warn("Skipping null notification message");
            return;
        }
        log.info("Publishing notification for user {} with title {}", message.getUserId(), message.getTitle());
        rabbitTemplate.convertAndSend(properties.getExchange(), properties.getRoutingKey(), message);
    }
}
