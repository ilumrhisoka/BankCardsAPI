package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.service.card.CardEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardDtoMapper {

    private final CardEncryptionService cardEncryptionService;

    public CardResponseDto toCardResponseDto(Card card) {
        if(card == null) {
            return null;
        }
        CardResponseDto dto = new CardResponseDto();
        dto.setId(card.getId());
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber((card.getCardNumber())));
        dto.setCardHolder(card.getCardHolder());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardStatus(card.getCardStatus());
        dto.setBalance(card.getBalance());
        if(card.getUser()!= null){
            dto.setUsername(card.getUser().getUsername());
        }
        return dto;
    }
}