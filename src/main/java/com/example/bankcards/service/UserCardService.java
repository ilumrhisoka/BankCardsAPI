package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserCardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public List<CardResponseDto> getUserCards(String username) {
        List<Card> cards = cardRepository.findByUserUsername(username);
        return cards.stream()
                .map(this::mapToMaskedResponseDto)
                .collect(Collectors.toList());
    }
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
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied: Card doesn't belong to user");
        }
        if (card.getCardStatus() != CardStatus.BLOCKED) {
            throw new RuntimeException("Card is not blocked");
        }
        log.info("User {} requested unblock for card {}", username, CardMaskingUtil.maskCardNumber(card.getCardNumber()));
    }
    public CardResponseDto getUserCard(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied: Card doesn't belong to user");
        }

        return mapToMaskedResponseDto(card);
    }

    private CardResponseDto mapToMaskedResponseDto(Card card) {
        CardResponseDto dto = new CardResponseDto();
        dto.setId(card.getId());
        dto.setCardNumber(CardMaskingUtil.maskCardNumber(card.getCardNumber()));
        dto.setCardHolder(card.getCardHolder());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardStatus(card.getCardStatus());
        dto.setBalance(card.getBalance());
        dto.setUsername(card.getUser().getUsername());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        return dto;
    }
}