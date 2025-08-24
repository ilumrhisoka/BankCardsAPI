package com.example.bankcards.controller.user;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponseDto;
import com.example.bankcards.service.transfer.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user/transfers")
@RequiredArgsConstructor
@Tag(name = "User Transfers", description = "Operations related to managing user's money transfers")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "Create a new money transfer",
            description = "Allows an authenticated user to initiate a new money transfer between cards.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body, insufficient funds, or other business rule violation",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required or card not owned by user",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Source or destination card not found",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransfer(@Valid @RequestBody TransferRequest request,
                                                              Authentication authentication) {
        String username = authentication.getName();
        TransferResponseDto transfer = transferService.createTransfer(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    @Operation(summary = "Get all transfers for the current user",
            description = "Retrieves a list of all money transfers initiated by or received by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transfers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User access required",
                    content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<List<TransferResponseDto>> getMyTransfers(Authentication authentication) {
        String username = authentication.getName();
        List<TransferResponseDto> transfers = transferService.getUserTransfers(username);
        return ResponseEntity.ok(transfers);
    }

    @Operation(summary = "Get transfers for a specific card",
            description = "Retrieves a list of transfers associated with a specific card owned by the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of card transfers",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Card not owned by user or user access required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID",
                    content = @Content)
    })
    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransferResponseDto>> getCardTransfers(@PathVariable Long cardId,
                                                                      Authentication authentication) {
        String username = authentication.getName();
        List<TransferResponseDto> transfers = transferService.getCardTransfers(cardId, username);
        return ResponseEntity.ok(transfers);
    }

    @Operation(summary = "Get a specific transfer by ID",
            description = "Retrieves details of a specific transfer by its ID, if it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransferResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Transfer not associated with user or user access required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Transfer not found with the given ID",
                    content = @Content)
    })
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponseDto> getTransfer(@PathVariable Long transferId,
                                                           Authentication authentication) {
        String username = authentication.getName();
        TransferResponseDto transfer = transferService.getTransfer(transferId, username);
        return ResponseEntity.ok(transfer);
    }
}