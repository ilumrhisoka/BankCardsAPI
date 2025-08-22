package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min=3, max = 50, message = "Username must be between 3 and 50 charactest")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
