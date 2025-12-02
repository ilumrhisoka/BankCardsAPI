package com.example.bankcards.repository;

import com.example.bankcards.model.entity.SupportTicket;
import com.example.bankcards.model.entity.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    // Найти все тикеты пользователя
    List<SupportTicket> findByUserUsernameOrderByCreatedAtDesc(String username);

    // Найти открытые тикеты для админов
    List<SupportTicket> findByStatusIn(List<TicketStatus> statuses);
}