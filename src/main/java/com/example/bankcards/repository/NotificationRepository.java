package com.example.bankcards.repository;

import com.example.bankcards.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);
    List<Notification> findByUserUsernameAndIsReadFalseOrderByCreatedAtDesc(String username);
}