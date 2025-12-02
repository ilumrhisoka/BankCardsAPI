package com.example.bankcards.controller.admin;

import com.example.bankcards.model.dto.ticket.TicketResponseDto;
import com.example.bankcards.service.support.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for administrative operations related to support tickets.
 * Access restricted to users with ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/tickets")
@Tag(name = "Admin Support Ticket Management", description = "Operations for administrators to manage and process support tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupportTicketController {

    private final SupportTicketService ticketService;

    @Operation(summary = "Get all open and in-progress tickets")
    @GetMapping("/open")
    public ResponseEntity<List<TicketResponseDto>> getOpenTickets() {
        List<TicketResponseDto> tickets = ticketService.getOpenTickets();
        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Assign a ticket to the currently authenticated admin")
    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponseDto> assignTicketToMe(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String adminUsername = authentication.getName();
        TicketResponseDto assignedTicket = ticketService.assignTicket(ticketId, adminUsername);
        return ResponseEntity.ok(assignedTicket);
    }

    // В реальной системе может быть метод для назначения любому админу, но для простоты используем "назначить мне".

    @Operation(summary = "Close a specific ticket")
    @PostMapping("/{ticketId}/close")
    public ResponseEntity<TicketResponseDto> closeTicket(@PathVariable Long ticketId) {
        TicketResponseDto closedTicket = ticketService.closeTicket(ticketId);
        return ResponseEntity.ok(closedTicket);
    }
}