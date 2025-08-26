package com.example.bankcards.controller.admin;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.card.CardCreateRequest;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.dto.card.CardUpdateRequest;
import com.example.bankcards.exception.card.CardOwnershipException;
import com.example.bankcards.service.card.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.bankcards.exception.dto.ErrorResponse;

/**
 * REST controller for managing bank cards by administrators.
 * This controller provides endpoints for creating, retrieving, updating, deleting,
 * blocking, and activating bank cards.
 * All operations require the authenticated user to have the 'ROLE_ADMIN' authority.
 */

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin Card Management", description = "Operations related to managing bank cards by administrators")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    /**
     * Creates a new bank card with specified details.
     * This operation is accessible only by administrators.
     *
     * @param request The request body containing details for the new card, including user ID,
     *                card number, card holder name, expiry date, and initial balance.
     * @return A {@link ResponseEntity} containing the created card details
     *         ({@link CardResponseDto}) and HTTP status 201 (Created).
     * @throws UserNotFoundException (HTTP 404) if the user specified by {@code userId} in the request is not found.
     * @throws IllegalArgumentException (HTTP 400) if the card number in the request is invalid.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Create a new bank card",
            description = "Allows administrators to create a new bank card with specified details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or data validation failed.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))), // Для общих ошибок валидации
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class))),
            @ApiResponse(responseCode = "404", description = "User not found for card creation.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserNotFoundException.class)))
    })
    @PostMapping
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardCreateRequest request){
        CardResponseDto card = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    /**
     * Retrieves a paginated list of all bank cards.
     * This operation is accessible only by administrators.
     *
     * @param pageable Pagination information (page number, size, sort order).
     * @return A {@link ResponseEntity} containing a {@link Page} of card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Get all bank cards with pagination",
            description = "Retrieves a paginated list of all bank cards. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getAllCards(Pageable pageable) {
        Page<CardResponseDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    /**
     * Retrieves details of a specific bank card by its ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to retrieve.
     * @return A {@link ResponseEntity} containing the card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Get a bank card by ID",
            description = "Retrieves details of a specific bank card by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long id) {
        CardResponseDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    /**
     * Updates details of an existing bank card by its ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to update.
     * @param request The request body containing updated card details.
     * @return A {@link ResponseEntity} containing the updated card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Update an existing bank card",
            description = "Allows administrators to update details of an existing bank card by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or data validation failed.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))), // Для общих ошибок валидации
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Long id,
                                                      @Valid @RequestBody CardUpdateRequest request) {
        CardResponseDto card = cardService.updateCard(id, request);
        return ResponseEntity.ok(card);
    }

    /**
     * Deletes a bank card permanently by its ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to delete.
     * @return A {@link ResponseEntity} with no content and HTTP status 204 (No Content)
     *         upon successful deletion.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Delete a bank card by ID",
            description = "Deletes a bank card permanently by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Blocks a specific bank card by its ID, preventing further transactions.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to block.
     * @return A {@link ResponseEntity} containing the blocked card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardStatusException (HTTP 400) if the card is already blocked or cannot be blocked in its current state.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Block a bank card",
            description = "Blocks a specific bank card by its ID, preventing further transactions. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card blocked successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Card is already blocked or cannot be blocked in its current state.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardStatusException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @PostMapping("/{id}/block")
    public ResponseEntity<CardResponseDto> blockCard(@PathVariable Long id) {
        CardResponseDto card = cardService.blockCard(id);
        return ResponseEntity.ok(card);
    }

    /**
     * Activates a previously blocked or inactive bank card by its ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to activate.
     * @return A {@link ResponseEntity} containing the activated card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardStatusException (HTTP 400) if the card is already active or cannot be activated in its current state.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Activate a bank card",
            description = "Activates a previously blocked or inactive bank card by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card activated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Card is already active or cannot be activated in its current state.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardStatusException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<CardResponseDto> activateCard(@PathVariable Long id) {
        CardResponseDto card = cardService.activateCard(id);
        return ResponseEntity.ok(card);
    }

    /**
     * Approves a pending block request for a specific bank card by its ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to approve blocking for.
     * @return A {@link ResponseEntity} containing the blocked card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardStatusException (HTTP 400) if the card is not in PENDING_BLOCK state.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Approve a card block request",
            description = "Approves a pending block request for a specific bank card by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card block request approved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Card is not in PENDING_BLOCK state.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardStatusException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @PostMapping("/{id}/approve-block")
    public ResponseEntity<CardResponseDto> approveBlockRequest(@PathVariable Long id) {
        CardResponseDto card = cardService.approveBlockRequest(id);
        return ResponseEntity.ok(card);
    }

    /**
     * Approves a pending unblock request for a specific bank card by its ID.
     * This operation is accessible only by administrators.
     *
     * @param id The ID of the card to approve unblocking for.
     * @return A {@link ResponseEntity} containing the activated card details
     *         ({@link CardResponseDto}) and HTTP status 200 (OK).
     * @throws CardStatusException (HTTP 400) if the card is not in PENDING_UNBLOCK state.
     * @throws CardNotFoundException (HTTP 404) if no card is found with the given ID.
     * @throws CardOwnershipException (HTTP 403) if the authenticated user does not have 'ROLE_ADMIN' authority.
     */
    @Operation(summary = "Approve a card unblock request",
            description = "Approves a pending unblock request for a specific bank card by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card unblock request approved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Card is not in PENDING_UNBLOCK state.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardStatusException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardOwnershipException.class)))
    })
    @PostMapping("/{id}/approve-unblock")
    public ResponseEntity<CardResponseDto> approveUnblockRequest(@PathVariable Long id) {
        CardResponseDto card = cardService.approveUnblockRequest(id);
        return ResponseEntity.ok(card);
    }
}