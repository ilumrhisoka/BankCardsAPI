package com.example.bankcards.model.dto.card;

import com.example.bankcards.model.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for card response.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for card response, representing bank card information.")
public class CardResponseDto {
    @Schema(description = "Unique card identifier.", example = "1")
    private Long id;
    @Schema(description = "Bank card number.", example = "1234567890123456")
    private String cardNumber;
    @Schema(description = "Name of the card holder.", example = "JOHN DOE")
    private String cardHolder;
    @Schema(description = "Card expiration date.", example = "2028-12-31")
    private LocalDate expiryDate;
    @Schema(description = "Current status of the card (e.g., ACTIVE, BLOCKED).", example = "ACTIVE")
    private CardStatus cardStatus;
    @Schema(description = "Current balance of the card.", example = "950.50")
    private BigDecimal balance;
    @Schema(description = "Username of the card owner.", example = "testuser")
    private String username;
}