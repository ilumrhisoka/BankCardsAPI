package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting between {@link com.example.bankcards.model.entity.Card} entities
 * and their corresponding DTOs ({@link com.example.bankcards.model.dto.card.CardResponseDto}).
 * This interface uses MapStruct for automatic mapping generation.
 */
@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(target = "username", source = "account.user.username") // Corrected mapping path
    CardResponseDto toCardResponseDto(Card card);
}