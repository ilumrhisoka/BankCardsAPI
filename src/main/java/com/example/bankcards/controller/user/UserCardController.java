package com.example.bankcards.controller.user;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.service.card.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.example.bankcards.exception.card.CardOwnershipException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;

/**
 * REST controller for managing user's bank cards.
 * This controller provides endpoints for retrieving user's cards, blocking/unblocking requests,
 * and getting the total balance across all user cards.
 * All operations require the authenticated user.
 */
@RestController
@RequestMapping("/api/user/cards")
@Tag(name = "User Card Management", description = "Operations related to managing user's bank cards")
@RequiredArgsConstructor
public class UserCardController {
    private final UserCardService userCardService;

    /**
     * Retrieves a paginated list of all bank cards owned by the authenticated user.
     *
     * @param authentication The authentication object containing the current user's details.
     * @param pageable Pagination information (page number, size, sort order).
     * @return A {@link ResponseEntity} containing a {@link Page} of card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have required access.
     */
    @Operation(summary = "Get all cards for the current user with pagination",
            description = "Retrieves a paginated list of all bank cards owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of user cards",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getUserCards(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<CardResponseDto> cards = userCardService.getUserCards(username, pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Retrieves details of a specific bank card by its ID, if it belongs to the authenticated user.
     *
     * @param cardId The ID of the card to retrieve.
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} containing the card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardOwnershipException (HTTP 403) if the card does not belong to the user or user access is required.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     */
    @Operation(summary = "Get a specific card by ID",
            description = "Retrieves details of a specific bank card by its ID, if it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class)))
    })
    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long cardId,
                                                       Authentication authentication) {
        String username = authentication.getName();
        CardResponseDto card = userCardService.getUserCardById(cardId, username);
        return ResponseEntity.ok(card);
    }

    /**
     * Submits a request to block a specific card owned by the authenticated user.
     * The actual blocking might be an asynchronous process or require admin approval.
     *
     * @param cardId The ID of the card to request blocking for.
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} with no content and HTTP status 200 (OK) upon successful request submission.
     * @throws CardStatusException (HTTP 400) if the card is already blocked or in a pending block state.
     * @throws CardOwnershipException (HTTP 403) if the card does not belong to the user or user access is required.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     */
    @Operation(summary = "Request to block a card",
            description = "Submits a request to block a specific card owned by the authenticated user. The actual blocking might be an asynchronous process or require admin approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card block request submitted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Card is already blocked or a block request is pending.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardStatusException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class)))
    })
    @PostMapping("/block/{cardId}")
    public ResponseEntity<?> requestBlockCard(@PathVariable Long cardId,
                                              Authentication authentication) {
        String username = authentication.getName();
        userCardService.requestBlock(cardId, username);
        return ResponseEntity.ok().build();
    }

    /**
     * Submits a request to unblock a specific card owned by the authenticated user.
     * The actual unblocking might be an asynchronous process or require admin approval.
     *
     * @param cardId The ID of the card to request unblocking for.
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} with no content and HTTP status 200 (OK) upon successful request submission.
     * @throws CardStatusException (HTTP 400) if the card is already active or in a pending unblock state.
     * @throws CardOwnershipException (HTTP 403) if the card does not belong to the user or user access is required.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     */
    @Operation(summary = "Request to unblock a card",
            description = "Submits a request to unblock a specific card owned by the authenticated user. The actual unblocking might be an asynchronous process or require admin approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card unblock request submitted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Card is already active or an unblock request is pending.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardStatusException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class)))
    })
    @PostMapping("/unblock/{cardId}")
    public ResponseEntity<?> requestUnblockCard(@PathVariable Long cardId,
                                                Authentication authentication) {
        String username = authentication.getName();
        userCardService.requestUnblock(cardId, username);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the sum of balances from all bank cards owned by the authenticated user.
     *
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} containing the total balance as a {@link BigDecimal}
     *         and HTTP status 200 (OK).
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have required access.
     */
    @Operation(summary = "Get total balance across all user cards",
            description = "Retrieves the sum of balances from all bank cards owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total balance retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BigDecimal.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(Authentication authentication) {
        String username = authentication.getName();
        BigDecimal balance = userCardService.getTotalBalance(username);
        return ResponseEntity.ok(balance);
    }
}