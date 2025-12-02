package com.example.bankcards.model.entity.enums;

/**
 * Defines the severity or category of a notification.
 */
public enum NotificationType {
    INFO,       // Информационное сообщение
    ALERT,      // Важное предупреждение (например, крупная транзакция)
    SECURITY,   // Уведомление о безопасности (например, смена пароля)
    SYSTEM,
    SUCCESS// Системное уведомление
}