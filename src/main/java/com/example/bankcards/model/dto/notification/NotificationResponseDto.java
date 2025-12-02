package com.example.bankcards.model.dto.notification;

import com.example.bankcards.model.entity.enums.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response DTO for displaying notification details to the user.
 */
@Data
public class NotificationResponseDto {
    private Long id;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private Boolean isRead;
}