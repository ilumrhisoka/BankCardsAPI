package com.example.bankcards.util;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardDtoMapper {

    public CardResponseDto toCardResponseDto(Card card) {
        if(card == null) {
            return null;
        }
        CardResponseDto dto = new CardResponseDto();
        dto.setId(card.getId());
        dto.setCardNumber(CardMaskingUtil.maskCardNumber(card.getCardNumber()));
        dto.setCardHolder(card.getCardHolder());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardStatus(card.getCardStatus());
        dto.setBalance(card.getBalance());
        dto.setUsername(card.getUser().getUsername());
        return dto;
    }
}
