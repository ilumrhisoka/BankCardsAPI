package com.example.bankcards.service.card;

import com.example.bankcards.dto.card.CardCreateRequest;
import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.card.CardUpdateRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.CardDtoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionService cardEncryptionService;
    private final CardDtoMapper cardDtoMapper;

    @Transactional
    public CardResponseDto createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encryptedCardNumber = cardEncryptionService.encryptCardNumber(request.getCardNumber());

        Card card = new Card();
        card.setCardNumber(encryptedCardNumber);
        card.setCardHolder(request.getCardHolder());
        card.setExpiryDate(request.getExpiryDate());
        card.setBalance(request.getBalance());
        card.setCardStatus(CardStatus.ACTIVE);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        log.info("Created card with ID: {}", savedCard.getId());

        return cardDtoMapper.toCardResponseDto(savedCard);
    }

    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);
        return cards.map(cardDtoMapper::toCardResponseDto);
    }

    public CardResponseDto getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        return cardDtoMapper.toCardResponseDto(card);
    }

    @Transactional
    public CardResponseDto updateCard(Long id, CardUpdateRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (request.getCardHolder() != null) {
            card.setCardHolder(request.getCardHolder());
        }
        if (request.getExpiryDate() != null) {
            card.setExpiryDate(request.getExpiryDate());
        }
        if (request.getCardStatus() != null) {
            card.setCardStatus(request.getCardStatus());
        }
        if (request.getBalance() != null) {
            card.setBalance(request.getBalance());
        }

        Card savedCard = cardRepository.save(card);
        log.info("Updated card with ID: {}", savedCard.getId());

        return cardDtoMapper.toCardResponseDto(savedCard);
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        cardRepository.delete(card);
        log.info("Deleted card with ID: {}", id);
    }

    @Transactional
    public CardResponseDto blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setCardStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        log.info("Blocked card with ID: {}", id);
        return cardDtoMapper.toCardResponseDto(savedCard);
    }

    @Transactional
    public CardResponseDto activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setCardStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Activated card with ID: {}", id);
        return cardDtoMapper.toCardResponseDto(savedCard);
    }

}