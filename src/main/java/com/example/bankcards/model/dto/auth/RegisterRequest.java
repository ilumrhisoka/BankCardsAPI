package com.example.bankcards.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "DTO for new user registration request.")
public class RegisterRequest {
    @NotBlank(message = "Username cannot be empty") // Пример валидации
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters") // Пример валидации
    @Schema(description = "Username for registration.", example = "newuser")
    private String username;

    @NotBlank(message = "Email cannot be empty") // <-- Добавь это
    @Email(message = "Invalid email format")      // <-- И это
    @Schema(description = "User's email address.", example = "newuser@example.com")
    private String email;

    @NotBlank(message = "Password cannot be empty") // <-- Добавь это
    @Size(min = 8, message = "Password must be at least 8 characters long") // Пример валидации
    @Schema(description = "Password for the new user.", example = "securepassword")
    private String password;
}
