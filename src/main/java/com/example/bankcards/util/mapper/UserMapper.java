package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.user.UserResponseDto;
import com.example.bankcards.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "avatarUrl", source = "username", qualifiedByName = "generateAvatarUrl")
    UserResponseDto toUserResponseDto(User user);

    @Named("generateAvatarUrl")
    default String generateAvatarUrl(String username) {
        // Генерируем уникального робота для каждого юзера
        return "https://robohash.org/" + username + "?set=set1&bgset=bg2&size=150x150";
    }
}