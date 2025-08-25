package com.example.bankcards.service.card;

import com.example.bankcards.exception.card.CardNotBlockedException;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.mapper.CardDtoMapper;
import com.example.bankcards.util.CardMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserCardService {

    private final CardDtoMapper cardDtoMapper;
    private final CardRepository cardRepository;

    public BigDecimal getTotalBalance(String username) {
        List<Card> cards = cardRepository.findByUserUsername(username);
        return cards.stream()
                .filter(card -> card.getCardStatus() == CardStatus.ACTIVE)
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void requestUnblock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Card doesn't belong to user");
        }
        if (card.getCardStatus() != CardStatus.BLOCKED) {
            throw new CardNotBlockedException("Card is not blocked");
        }
        log.info("User {} requested unblock for card {}", username, CardMaskingUtil.maskCardNumber(card.getCardNumber()));
    }

    @Transactional
    public void requestBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Card doesn't belong to user");
        }
        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new CardBlockedException("Card is already blocked");
        }
        log.info("User {} requested block for card {}", username, CardMaskingUtil.maskCardNumber(card.getCardNumber()));
    }

    public Page<CardResponseDto> getUserCards(String username, Pageable pageable) {
        Page<Card> cardsPage = cardRepository.findByUserUsernamePageable(username, pageable);
        return cardsPage.map(cardDtoMapper::toCardResponseDto);
    }

    public CardResponseDto getUserCardById(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        if(!card.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Card doesn't belong to user");
        }
        return cardDtoMapper.toCardResponseDto(card);
    }
}