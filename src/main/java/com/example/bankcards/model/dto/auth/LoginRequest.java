package com.example.bankcards.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for user login request.
 */
@Getter
@Setter
@Schema(description = "DTO for user login request.")
public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min=3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username for login.", example = "testuser")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "User password.", example = "password123")
    private String password;
}
