package com.example.bankcards.controller.user;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.transfer.InvalidTransferException;
import com.example.bankcards.exception.transfer.TransferNotFoundException;
import com.example.bankcards.model.dto.transfer.TransferRequest;
import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.service.transfer.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for managing user's money transfers.
 * This controller provides endpoints for creating transfers, retrieving transfers
 * for the authenticated user, and retrieving transfers for a specific card owned by the user.
 * All operations require the authenticated user to have the 'ROLE_USER' authority.
 */
@RestController
@RequestMapping("/api/user/transfers")
@RequiredArgsConstructor
@Tag(name = "User Transfers", description = "Operations related to managing user's money transfers")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class TransferController {

    private final TransferService transferService;

    /**
     * Allows an authenticated user to initiate a new money transfer between cards.
     *
     * @param request The request body containing details for the transfer, including
     *                source card ID, destination card number, and amount.
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} containing the created transfer details
     *         ({@link TransferResponseDto}) and HTTP status 201 (Created).
     * @throws InvalidTransferException (HTTP 400) if attempting to transfer to the same card.
     * @throws InsufficientFundsException (HTTP 400) if the source card has insufficient funds.
     * @throws CardStatusException (HTTP 400) if the source or destination card is not active.
     * @throws AccessDeniedException (HTTP 403) if the authenticated user does not have 'ROLE_USER' authority
     *         or if the source card does not belong to the user.
     * @throws CardNotFoundException (HTTP 404) if the source or destination card is not found.
     */
    @Operation(summary = "Create a new money transfer",
            description = "Allows an authenticated user to initiate a new money transfer between cards.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid transfer request (e.g., same card, inactive card, insufficient funds).",
                    content = @Content(mediaType = "application/json", schema = @Schema(oneOf = {InvalidTransferException.class, InsufficientFundsException.class, CardStatusException.class}))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required or card does not belong to the user.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessDeniedException.class))),
            @ApiResponse(responseCode = "404", description = "Source or destination card not found.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class)))
    })
    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransfer(@Valid @RequestBody TransferRequest request,
                                                              Authentication authentication) {
        String username = authentication.getName();
        TransferResponseDto transfer = transferService.createTransfer(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    /**
     * Retrieves a list of all money transfers initiated by or received by the authenticated user.
     *
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} containing a list of transfer details
     *         ({@link TransferResponseDto}) and HTTP status 200 (OK).
     * @throws AccessDeniedException (HTTP 403) if the authenticated user does not have 'ROLE_USER' authority.
     */
    @Operation(summary = "Get all transfers for the current user",
            description = "Retrieves a list of all money transfers initiated by or received by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transfers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessDeniedException.class)))
    })
    @GetMapping("/my")
    public ResponseEntity<List<TransferResponseDto>> getMyTransfers(Authentication authentication) {
        String username = authentication.getName();
        List<TransferResponseDto> transfers = transferService.getUserTransfers(username);
        return ResponseEntity.ok(transfers);
    }

    /**
     * Retrieves a list of transfers associated with a specific card owned by the authenticated user.
     *
     * @param cardId The ID of the card for which to retrieve transfers.
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} containing a list of transfer details
     *         ({@link TransferResponseDto}) and HTTP status 200 (OK).
     * @throws AccessDeniedException (HTTP 403) if the card does not belong to the user or user access is required.
     * @throws CardNotFoundException (HTTP 404) if the card is not found with the given ID.
     */
    @Operation(summary = "Get transfers for a specific card",
            description = "Retrieves a list of transfers associated with a specific card owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of card transfers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card does not belong to user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessDeniedException.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardNotFoundException.class)))
    })
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransferResponseDto>> getCardTransfers(@PathVariable Long cardId,
                                                                      Authentication authentication) {
        String username = authentication.getName();
        List<TransferResponseDto> transfers = transferService.getCardTransfers(cardId, username);
        return ResponseEntity.ok(transfers);
    }

    /**
     * Retrieves details of a specific transfer by its ID, if it belongs to the authenticated user.
     *
     * @param transferId The ID of the transfer to retrieve.
     * @param authentication The authentication object containing the current user's details.
     * @return A {@link ResponseEntity} containing the transfer details
     *         ({@link TransferResponseDto}) and HTTP status 200 (OK).
     * @throws AccessDeniedException (HTTP 403) if the transfer is not associated with the user.
     * @throws TransferNotFoundException (HTTP 404) if the transfer is not found with the given ID.
     */
    @Operation(summary = "Get a specific transfer by ID",
            description = "Retrieves details of a specific transfer by its ID, if it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Transfer not associated with user or user access required.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AccessDeniedException.class))),
            @ApiResponse(responseCode = "404", description = "Transfer not found with the given ID.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferNotFoundException.class)))
    })
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponseDto> getTransfer(@PathVariable Long transferId,
                                                           Authentication authentication) {
        String username = authentication.getName();
        TransferResponseDto transfer = transferService.getTransfer(transferId, username);
        return ResponseEntity.ok(transfer);
    }
}