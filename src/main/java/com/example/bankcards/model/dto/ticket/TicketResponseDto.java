package com.example.bankcards.model.dto.ticket;

import com.example.bankcards.model.entity.enums.TicketPriority;
import com.example.bankcards.model.entity.enums.TicketStatus;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Response DTO for displaying support ticket details.
 */
@Data
public class TicketResponseDto {
    private Long id;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private String createdByUsername;
    private String assignedToAdminUsername;
    private LocalDateTime createdAt;
}