package com.example.bankcards.controller.user;

import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.service.card.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.bankcards.exception.dto.ErrorResponse;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/user/cards")
@Tag(name = "User Card Management", description = "Operations related to managing user's bank cards")
@RequiredArgsConstructor
public class UserCardController {
    private final UserCardService userCardService;

    @Operation(summary = "Get all cards for the current user with pagination",
            description = "Retrieves a paginated list of all bank cards owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of user cards",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))), // Springdoc обычно корректно обрабатывает Page<T>
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getUserCards(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<CardResponseDto> cards = userCardService.getUserCards(username, pageable);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Get a specific card by ID",
            description = "Retrieves details of a specific bank card by its ID, if it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long cardId,
                                                       Authentication authentication) {
        String username = authentication.getName();
        CardResponseDto card = userCardService.getUserCardById(cardId, username);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Request to block a card",
            description = "Submits a request to block a specific card owned by the authenticated user. The actual blocking might be an asynchronous process or require admin approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card block request submitted successfully",
                    content = @Content(mediaType = "application/json")), // Для 200 OK без тела ответа, можно оставить content без schema.
            @ApiResponse(responseCode = "400", description = "Card is already blocked or cannot be blocked in its current state.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/block/{cardId}")
    public ResponseEntity<?> requestBlockCard(@PathVariable Long cardId,
                                              Authentication authentication) {
        String username = authentication.getName();
        userCardService.requestBlock(cardId, username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Request to unblock a card",
            description = "Submits a request to unblock a specific card owned by the authenticated user. The actual unblocking might be an asynchronous process or require admin approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card unblock request submitted successfully",
                    content = @Content(mediaType = "application/json")), // Для 200 OK без тела ответа, можно оставить content без schema.
            @ApiResponse(responseCode = "400", description = "Card is already unblocked or cannot be unblocked in its current state.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/unblock/{cardId}")
    public ResponseEntity<?> requestUnblockCard(@PathVariable Long cardId,
                                              Authentication authentication) {
        String username = authentication.getName();
        userCardService.requestUnblock(cardId, username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get total balance across all user cards",
            description = "Retrieves the sum of balances from all bank cards owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total balance retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(Authentication authentication) {
        String username = authentication.getName();
        BigDecimal balance = userCardService.getTotalBalance(username);
        return ResponseEntity.ok(balance);
    }
}
