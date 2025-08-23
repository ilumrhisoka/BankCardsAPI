package com.example.bankcards.dto.card;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {
    private Long id;
    private String cardNumber;
    private String cardHolder;
    private LocalDate expiryDate;
    private CardStatus cardStatus;
    private BigDecimal balance;
    private String username;
}