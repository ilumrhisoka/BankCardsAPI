package com.example.bankcards.controller.user;

import com.example.bankcards.model.dto.ticket.TicketCreateRequest;
import com.example.bankcards.model.dto.ticket.TicketResponseDto;
import com.example.bankcards.service.support.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user interaction with the support ticket system.
 */
@RestController
@RequestMapping("/api/user/tickets")
@Tag(name = "User Support Tickets", description = "Operations for creating and viewing user support tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService ticketService;

    @Operation(summary = "Create a new support ticket")
    @PostMapping
    public ResponseEntity<TicketResponseDto> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        TicketResponseDto newTicket = ticketService.createTicket(request, username);
        return new ResponseEntity<>(newTicket, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all support tickets created by the current user")
    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getMyTickets(Authentication authentication) {
        String username = authentication.getName();
        List<TicketResponseDto> tickets = ticketService.getMyTickets(username);
        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Get details of a specific ticket")
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDto> getTicketById(
            @PathVariable Long ticketId,
            Authentication authentication) {
        String username = authentication.getName();
        TicketResponseDto ticket = ticketService.getTicketById(ticketId, username);
        return ResponseEntity.ok(ticket);
    }
}