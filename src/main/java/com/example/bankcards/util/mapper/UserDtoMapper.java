package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}