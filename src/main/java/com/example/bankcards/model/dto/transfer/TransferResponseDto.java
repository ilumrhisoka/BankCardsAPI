package com.example.bankcards.model.dto.transfer;

import com.example.bankcards.model.entity.enums.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for transfer response, representing information about a completed transfer.")
public class TransferResponseDto {
    @Schema(description = "Unique transfer identifier.", example = "1")
    private Long id;
    @Schema(description = "Number of the card from which the transfer was made.", example = "1234567890123456")
    private String fromCardNumber;
    @Schema(description = "Number of the card to which the transfer was made.", example = "9876543210987654")
    private String toCardNumber;
    @Schema(description = "Amount of the transfer.", example = "50.00")
    private BigDecimal amount;
    @Schema(description = "Status of the transfer (e.g., COMPLETED, PENDING, FAILED).", example = "COMPLETED")
    private TransferStatus status;
    @Schema(description = "Date and time when the transfer occurred.", example = "2023-10-26T10:30:00")
    private LocalDateTime transferDate;
    @Schema(description = "Date and time when the transfer record was created.", example = "2023-10-26T10:29:55")
    private LocalDateTime createdAt;
}