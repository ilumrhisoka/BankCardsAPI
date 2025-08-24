package com.example.bankcards.controller.admin;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.CardUpdateRequest;
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


@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@Tag(name = "Admin Card Management", description = "Operations related to managing bank cards by administrators")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCardController {

    private final CardService cardService;

    @Operation(summary = "Create a new bank card",
            description = "Allows administrators to create a new bank card with specified details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Card created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or data validation failed",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CardCreateRequest request){
        CardResponseDto card = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @Operation(summary = "Get all bank cards with pagination",
            description = "Retrieves a paginated list of all bank cards. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of cards",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))), // Springdoc обычно корректно обрабатывает Page<T>
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getAllCards(Pageable pageable) {
        Page<CardResponseDto> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Get a bank card by ID",
            description = "Retrieves details of a specific bank card by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card found and retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long id) {
        CardResponseDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Update an existing bank card",
            description = "Allows administrators to update details of an existing bank card by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or data validation failed",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable Long id,
                                                      @Valid @RequestBody CardUpdateRequest request) {
        CardResponseDto card = cardService.updateCard(id, request);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Delete a bank card by ID",
            description = "Deletes a bank card permanently by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Card deleted successfully",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Block a bank card",
            description = "Blocks a specific bank card by its ID, preventing further transactions. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card blocked successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Card is already blocked or cannot be blocked in its current state",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @PostMapping("/{id}/block")
    public ResponseEntity<CardResponseDto> blockCard(@PathVariable Long id) {
        CardResponseDto card = cardService.blockCard(id);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Activate a bank card",
            description = "Activates a previously blocked or inactive bank card by its ID. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Card activated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Card is already active or cannot be activated in its current state",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Card not found with the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required",
                    content = @Content)
    })
    @PostMapping("/{id}/activate")
    public ResponseEntity<CardResponseDto> activateCard(@PathVariable Long id) {
        CardResponseDto card = cardService.activateCard(id);
        return ResponseEntity.ok(card);
    }


}
