package com.example.bankcards.model.dto.account;

import com.example.bankcards.model.entity.enums.AccountType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO containing account details.
 */
@Data
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private AccountType accountType;
    private Long userId;
    private LocalDateTime createdAt;
}