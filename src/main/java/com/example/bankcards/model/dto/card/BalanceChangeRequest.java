package com.example.bankcards.model.dto.card;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BalanceChangeRequest {
    private BigDecimal amount;
}