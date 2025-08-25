package com.example.bankcards.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for user login request.")
public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min=3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username for login.", example = "testuser")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Schema(description = "User password.", example = "password123")
    private String password;
}
