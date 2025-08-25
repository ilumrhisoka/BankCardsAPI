package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "username", source = "card.user.username")
    CardResponseDto toCardResponseDto(Card card);
}