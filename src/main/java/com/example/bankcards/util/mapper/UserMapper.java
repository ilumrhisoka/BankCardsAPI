package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.entity.User;
import org.mapstruct.Mapper;

/**
 * Mapper interface for converting between {@link com.example.bankcards.model.entity.User} entities
 * and their corresponding DTOs ({@link com.example.bankcards.model.dto.user.UserResponseDto}).
 * This interface uses MapStruct for automatic mapping generation.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);
}