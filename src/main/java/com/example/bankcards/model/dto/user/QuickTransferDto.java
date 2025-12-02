package com.example.bankcards.model.dto.user;

import lombok.Data;

@Data
public class QuickTransferDto {
    private Long id;
    private String name;
    private String cardNumber;
}