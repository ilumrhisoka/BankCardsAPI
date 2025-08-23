package com.example.bankcards.dto.card;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardUpdateRequest {
    private String cardHolder;
    private LocalDate expiryDate;
    private CardStatus cardStatus;
    private BigDecimal balance;
}
