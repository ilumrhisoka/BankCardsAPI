package com.example.bankcards.model.dto.card;

import com.example.bankcards.model.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for updating an existing card.
 */
@Getter
@Setter
@Schema(description = "DTO for updating bank card information request.")
public class CardUpdateRequest {
    @Schema(description = "New name of the card holder.", example = "JANE DOE")
    private String cardHolder;
    @Schema(description = "New card expiration date.", example = "2030-01-01")
    private LocalDate expiryDate;
    @Schema(description = "New status of the card (e.g., ACTIVE, BLOCKED).", example = "BLOCKED")
    private CardStatus cardStatus;
    @Schema(description = "New balance of the card.", example = "1200.00")
    private BigDecimal balance;
}
