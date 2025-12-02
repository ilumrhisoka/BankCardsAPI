package com.example.bankcards.model.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new support ticket.
 */
@Data
public class TicketCreateRequest {
    @NotBlank
    @Size(max = 150)
    private String subject;

    @NotBlank
    private String description;
}