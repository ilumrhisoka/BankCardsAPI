package com.example.bankcards.model.dto.user;

import com.example.bankcards.model.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating user information request.")
public class UserUpdateRequest {
    @NotBlank(message = "Username cannot be blank")
    @Schema(description = "New username.", example = "updateduser")
    private String username;
    @Email(message = "Invalid email format")
    @Schema(description = "New user email address.", example = "updated.email@example.com")
    private String email;
    @NotNull(message = "Role cannot be null")
    @Schema(description = "New user role (e.g., USER, ADMIN).", example = "ADMIN")
    private Role role;
}