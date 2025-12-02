package com.example.bankcards.model.dto.fee;

import com.example.bankcards.model.entity.enums.FeeType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for displaying service fee details.
 */
@Data
public class ServiceFeeResponseDto {
    private Long id;
    private Long accountId;
    private FeeType feeType;
    private BigDecimal amount;
    private LocalDateTime dateCharged;
    private Boolean isPaid;
}