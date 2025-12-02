package com.example.bankcards.model.entity;

import com.example.bankcards.model.entity.enums.TicketPriority;
import com.example.bankcards.model.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a support request (ticket) submitted by a user.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_support_tickets")
public class SupportTicket extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;

    /**
     * Optional: Admin user assigned to handle the ticket.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User adminAssigned;
}