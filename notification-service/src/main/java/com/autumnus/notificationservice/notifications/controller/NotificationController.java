package com.autumnus.notificationservice.notifications.controller;

import com.autumnus.notificationservice.common.context.RequestContextHolder;
import com.autumnus.notificationservice.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.notifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List notifications for the authenticated user")
    public ResponseEntity<List<NotificationResponse>> listNotifications() {
        final Long userId = resolveUserId();
        log.debug("Fetching notifications for userId={}", userId);
        final List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        final Long userId = resolveUserId();
        log.debug("Marking notification={} as read for userId={}", notificationId, userId);
        final NotificationResponse response = notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId() {
        final String userId = RequestContextHolder.getContext().getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("X-User-Id header is required");
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid user id supplied");
        }
    }
}
