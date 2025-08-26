package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-26T22:04:27+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class CardMapperImpl implements CardMapper {

    @Override
    public CardResponseDto toCardResponseDto(Card card) {
        if ( card == null ) {
            return null;
        }

        CardResponseDto cardResponseDto = new CardResponseDto();

        cardResponseDto.setUsername( cardUserUsername( card ) );
        cardResponseDto.setId( card.getId() );
        cardResponseDto.setCardHolder( card.getCardHolder() );
        cardResponseDto.setExpiryDate( card.getExpiryDate() );
        cardResponseDto.setCardStatus( card.getCardStatus() );
        cardResponseDto.setBalance( card.getBalance() );

        return cardResponseDto;
    }

    private String cardUserUsername(Card card) {
        if ( card == null ) {
            return null;
        }
        User user = card.getUser();
        if ( user == null ) {
            return null;
        }
        String username = user.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
