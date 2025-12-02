package com.example.bankcards.service.notification;

import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.model.dto.notification.NotificationResponseDto;
import com.example.bankcards.model.entity.Notification;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.NotificationType;
import com.example.bankcards.repository.NotificationRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserRepository userRepository; // Needed for creating notifications internally

    /**
     * Retrieves all notifications for the authenticated user, sorted by creation date.
     *
     * @param username The username of the recipient.
     * @return List of notification DTOs.
     */
    public List<NotificationResponseDto> getMyNotifications(String username) {
        return notificationRepository.findByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(notificationMapper::toNotificationResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves only unread notifications for the authenticated user.
     *
     * @param username The username of the recipient.
     * @return List of unread notification DTOs.
     */
    public List<NotificationResponseDto> getUnreadNotifications(String username) {
        return notificationRepository.findByUserUsernameAndIsReadFalseOrderByCreatedAtDesc(username).stream()
                .map(notificationMapper::toNotificationResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks a specific notification as read.
     *
     * @param notificationId The ID of the notification to mark.
     * @param username The username to verify ownership.
     */
    @Transactional
    public void markAsRead(Long notificationId, String username) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getUsername().equals(username)) {
            // Хотя это не ForbiddenException, мы используем ResourceNotFound, чтобы не раскрывать существование чужих уведомлений.
            throw new ResourceNotFoundException("Notification not found or access denied.");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
            log.info("Notification {} marked as read for user {}", notificationId, username);
        }
    }

    /**
     * Internal method to create and save a new notification.
     * This method would be called by other services (e.g., TransferService, CardService)
     * when an event occurs (e.g., successful transfer, card status change).
     */
    @Transactional
    public void createNotification(String username, String message, NotificationType type) {
        User user = userRepository.findByUsername(username)
                .orElse(null); // Если пользователь не найден, просто не отправляем уведомление

        if (user != null) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);
            notification.setType(type);
            notification.setIsRead(false);
            notificationRepository.save(notification);
            log.debug("Created notification for user {}: {}", username, message);
        }
    }
}