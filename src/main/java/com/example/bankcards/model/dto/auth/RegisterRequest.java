package com.example.bankcards.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO for new user registration request.")
public class RegisterRequest {
    @Schema(description = "Username for registration.", example = "newuser")
    private String username;
    @Schema(description = "User's email address.", example = "newuser@example.com")
    private String email;
    @Schema(description = "Password for the new user.", example = "securepassword")
    private String password;
}
