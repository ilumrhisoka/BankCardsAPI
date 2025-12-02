package com.example.bankcards.model.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for transfer funds between cards request.")
public class TransferRequest {
    @NotNull(message = "From card ID is required")
    @Schema(description = "ID of the card from which the transfer is made.", example = "1")
    private Long fromCardId;

    @Schema(description = "Number of the card to which the transfer is made.", example = "9876543210987654")
    private String toCardNumber;

    @Schema(description = "ID of the destination card (optional, used for internal transfers).", example = "2")
    private Long toCardId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Amount of the transfer.", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "Optional description of the transfer.", example = "Payment for dinner")
    private String description;
}