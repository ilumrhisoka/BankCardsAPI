package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);
}