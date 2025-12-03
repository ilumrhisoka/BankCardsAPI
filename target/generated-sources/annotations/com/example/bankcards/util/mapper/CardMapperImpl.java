package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Account;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T19:23:34+0300",
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

        cardResponseDto.setUsername( cardAccountUserUsername( card ) );
        cardResponseDto.setId( card.getId() );
        cardResponseDto.setCardHolder( card.getCardHolder() );
        cardResponseDto.setExpiryDate( card.getExpiryDate() );
        cardResponseDto.setCardStatus( card.getCardStatus() );
        cardResponseDto.setBalance( card.getBalance() );

        return cardResponseDto;
    }

    private String cardAccountUserUsername(Card card) {
        if ( card == null ) {
            return null;
        }
        Account account = card.getAccount();
        if ( account == null ) {
            return null;
        }
        User user = account.getUser();
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
