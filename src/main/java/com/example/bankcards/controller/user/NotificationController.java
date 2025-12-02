package com.example.bankcards.controller.user;

import com.example.bankcards.model.dto.notification.NotificationResponseDto;
import com.example.bankcards.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user notifications.
 */
@RestController
@RequestMapping("/api/user/notifications")
@Tag(name = "User Notifications", description = "Operations related to viewing and managing user notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications for the current user")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications(Authentication authentication) {
        String username = authentication.getName();
        List<NotificationResponseDto> notifications = notificationService.getMyNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get only unread notifications")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications(Authentication authentication) {
        String username = authentication.getName();
        List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(username);
        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Mark a specific notification as read")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {
        String username = authentication.getName();
        notificationService.markAsRead(notificationId, username);
        return ResponseEntity.ok().build();
    }
}