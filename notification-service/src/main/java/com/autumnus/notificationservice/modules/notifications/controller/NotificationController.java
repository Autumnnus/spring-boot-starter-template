package com.autumnus.notificationservice.modules.notifications.controller;

import com.autumnus.notificationservice.common.api.ApiResponse;
import com.autumnus.notificationservice.common.context.RequestContextHolder;
import com.autumnus.notificationservice.modules.notifications.dto.NotificationResponse;
import com.autumnus.notificationservice.modules.notifications.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> listNotifications() {
        final Long userId = resolveUserId();
        final List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok(notifications));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable("id") @NotNull Long id) {
        final Long userId = resolveUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    private Long resolveUserId() {
        final String userId = RequestContextHolder.getContext().getUserId();
        System.out.println("User Id" + userId);
        if (userId == null) {
            throw new IllegalStateException("Missing X-User-Id header");
        }
        return Long.parseLong(userId);
    }
}
