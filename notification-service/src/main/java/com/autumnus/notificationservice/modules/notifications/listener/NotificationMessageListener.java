package com.autumnus.notificationservice.modules.notifications.listener;

import com.autumnus.notificationservice.modules.notifications.messaging.NotificationMessage;
import com.autumnus.notificationservice.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${application.messaging.notifications.queue}")
    public void onMessage(NotificationMessage message) {
        System.out.println("onMessage " + message);
        if (message == null) {
            log.warn("Received null notification message");
            return;
        }
        log.info("Received notification for user {} with title {}", message.userId(), message.title());
        notificationService.save(message);
    }
}
