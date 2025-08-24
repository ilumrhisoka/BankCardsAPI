package com.example.bankcards.dto.transfer;

import com.example.bankcards.entity.enums.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseDto {
    private Long id;
    private String fromCardNumber;
    private String toCardNumber;
    private BigDecimal amount;
    private TransferStatus status;
    private LocalDateTime transferDate;
    private LocalDateTime createdAt;
}