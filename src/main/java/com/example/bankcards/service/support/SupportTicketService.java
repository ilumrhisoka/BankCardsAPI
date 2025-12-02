package com.example.bankcards.service.support;

import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.ticket.TicketCreateRequest;
import com.example.bankcards.model.dto.ticket.TicketResponseDto;
import com.example.bankcards.model.entity.SupportTicket;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.NotificationType;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.model.entity.enums.TicketPriority;
import com.example.bankcards.model.entity.enums.TicketStatus;
import com.example.bankcards.repository.SupportTicketRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.notification.NotificationService;
import com.example.bankcards.util.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user support tickets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMapper ticketMapper;
    private final NotificationService notificationService;

    /**
     * Creates a new support ticket for the authenticated user.
     *
     * @param request The ticket details.
     * @param username The username of the creator.
     * @return DTO of the created ticket.
     */
    @Transactional
    public TicketResponseDto createTicket(TicketCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM); // По умолчанию

        SupportTicket savedTicket = ticketRepository.save(ticket);
        log.info("New support ticket created by user {}. ID: {}", username, savedTicket.getId());
        return ticketMapper.toTicketResponseDto(savedTicket);
    }

    /**
     * Retrieves all tickets created by the authenticated user.
     */
    public List<TicketResponseDto> getMyTickets(String username) {
        return ticketRepository.findByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(ticketMapper::toTicketResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific ticket, verifying ownership.
     */
    public TicketResponseDto getTicketById(Long ticketId, String username) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: You do not own this ticket.");
        }
        return ticketMapper.toTicketResponseDto(ticket);
    }

    // --- Admin/Internal Logic (Example) ---

    /**
     * Retrieves all open and in-progress tickets (Admin view).
     */
    @Transactional
    public TicketResponseDto assignTicket(Long ticketId, String adminUsername) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found: " + adminUsername));

        // Проверка, что пользователь, которому назначается тикет, действительно администратор
        if (admin.getRole() != Role.ROLE_ADMIN) {
            throw new ForbiddenException("Cannot assign ticket: User " + adminUsername + " is not an administrator.");
        }

        ticket.setAdminAssigned(admin);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        SupportTicket savedTicket = ticketRepository.save(ticket);
        log.info("Support ticket {} assigned to admin {}", ticketId, adminUsername);

        // Уведомление пользователя о том, что тикет взят в работу
        notificationService.createNotification(
                ticket.getUser().getUsername(),
                String.format("Your ticket #%d has been assigned to a specialist and is now In Progress.", ticketId),
                NotificationType.INFO
        );

        return ticketMapper.toTicketResponseDto(savedTicket);
    }

    /**
     * Retrieves all open and in-progress tickets (Admin view).
     */
    public List<TicketResponseDto> getOpenTickets() {
        List<TicketStatus> openStatuses = List.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS);
        return ticketRepository.findByStatusIn(openStatuses).stream()
                .map(ticketMapper::toTicketResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Closes a ticket (Admin action).
     */
    @Transactional
    public TicketResponseDto closeTicket(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (ticket.getStatus() != TicketStatus.CLOSED) {
            ticket.setStatus(TicketStatus.CLOSED);
            ticket.setPriority(TicketPriority.LOW);
            SupportTicket savedTicket = ticketRepository.save(ticket);
            log.info("Support ticket {} closed by admin.", ticketId);

            // Уведомление пользователя о закрытии
            notificationService.createNotification(
                    ticket.getUser().getUsername(),
                    String.format("Your ticket #%d has been resolved and closed.", ticketId),
                    NotificationType.SUCCESS
            );

            return ticketMapper.toTicketResponseDto(savedTicket);
        }
        return ticketMapper.toTicketResponseDto(ticket);
    }


}