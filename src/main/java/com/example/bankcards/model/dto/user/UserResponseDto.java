package com.example.bankcards.model.dto.user;

import com.example.bankcards.model.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for user response.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO for user response, representing user information.")
public class UserResponseDto {
    @Schema(description = "Unique user identifier.", example = "1")
    private Long id;
    @Schema(description = "Username.", example = "testuser")
    private String username;
    @Schema(description = "User's email address.", example = "testuser@example.com")
    private String email;
    @Schema(description = "User's role (e.g., USER, ADMIN).", example = "USER")
    private Role role;
    @Schema(description = "Date and time when the user account was created.", example = "2023-01-15T09:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "Date and time when the user account was last updated.", example = "2023-10-20T14:45:00")
    private LocalDateTime updatedAt;
}