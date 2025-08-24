package com.example.bankcards.dto.user;

import com.example.bankcards.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;
    @Email(message = "Invalid email format")
    private String email;
    @NotNull(message = "Role cannot be null")
    private Role role;
}