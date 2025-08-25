package com.example.bankcards.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for authentication response, containing access tokens.")
public class AuthResponseDto {
    @Schema(description = "Access token (JWT) for authenticated requests.", example = "eyJhbGciOiJIUzI1Ni...")
    private String accessToken;
    @Schema(description = "Refresh token (JWT) for obtaining a new access token.", example = "eyJhbGciOiJIUzI1Ni...")
    private String refreshToken;
}