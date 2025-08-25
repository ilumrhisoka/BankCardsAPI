package com.example.bankcards.model.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for creating a new bank card request.")
public class CardCreateRequest {
    @NotBlank(message = "Card number is required")
    @Schema(description = "Unique bank card number.", example = "1234567890123456")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    @Schema(description = "Name of the card holder.", example = "JOHN DOE")
    private String cardHolder;

    @NotNull(message = "Expiry date is required")
    @Schema(description = "Card expiration date in YYYY-MM-DD format.", example = "2028-12-31")
    private LocalDate expiryDate;

    @NotNull(message = "Initial balance is required")
    @Positive(message = "Initial balance must be positive")
    @Schema(description = "Initial balance of the card.", example = "1000.00")
    private BigDecimal balance;

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user to whom the card belongs.", example = "1")
    private Long userId;
}