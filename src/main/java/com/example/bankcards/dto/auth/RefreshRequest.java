package com.example.bankcards.dto.auth;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}
