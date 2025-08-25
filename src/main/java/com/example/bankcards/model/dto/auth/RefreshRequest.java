package com.example.bankcards.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for refresh token request.
 */
@Getter
@Setter
@Schema(description = "DTO for refresh token request.")
public class RefreshRequest {
    @NotBlank
    @Schema(description = "Refresh token used to obtain a new access token.", example = "eyJhbGciOiJIUzI1Ni...")
    private String refreshToken;
}
