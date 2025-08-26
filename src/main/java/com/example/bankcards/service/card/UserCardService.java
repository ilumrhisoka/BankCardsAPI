package com.example.bankcards.service.card;

import com.example.bankcards.exception.card.CardOwnershipException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service class for managing bank card operations for regular users.
 * This service provides functionalities for users to view their cards,
 * request blocking/unblocking, and check their total balance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserCardService {

    private final CardMapper cardMapper;
    private final CardRepository cardRepository;
    private final CardEncryptionService cardEncryptionService;

    /**
     * Calculates the total balance across all active bank cards owned by a specific user.
     *
     * @param username The username of the user whose total balance is to be calculated.
     * @return A {@link BigDecimal} representing the sum of balances of all active cards.
     */
    public BigDecimal getTotalBalance(String username) {
        List<Card> cards = cardRepository.findByUserUsername(username);
        return cards.stream()
                .filter(card -> card.getCardStatus() == CardStatus.ACTIVE)
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Submits a request to unblock a specific card owned by the authenticated user.
     * Changes the card status to {@code ACTIVE_PENDING} to indicate a request.
     *
     * @param cardId The ID of the card to request unblocking for.
     * @param username The username of the user who owns the card.
     * @throws CardNotFoundException if no card is found with the given ID.
     * @throws CardOwnershipException if the card does not belong to the specified user.
     * @throws CardStatusException if the card is already active or in a pending unblock state.
     */
    @Transactional
    public void requestUnblock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new CardOwnershipException("Access denied: Card doesn't belong to user");
        }
        if (card.getCardStatus() != CardStatus.BLOCKED) {
            throw new CardStatusException("Card is not blocked");
        }
        log.info("User {} requested unblock for card {}", username, cardEncryptionService.getMaskedCardNumber(card.getCardNumber()));
        card.setCardStatus(CardStatus.PENDING_UNBLOCK);
    }

    /**
     * Submits a request to block a specific card owned by the authenticated user.
     * Changes the card status to {@code BLOCKED_PENDING} to indicate a request.
     *
     * @param cardId The ID of the card to request blocking for.
     * @param username The username of the user who owns the card.
     * @throws CardNotFoundException if no card is found with the given ID.
     * @throws CardOwnershipException if the card does not belong to the specified user.
     * @throws CardStatusException if the card is already blocked or in a pending block state.
     */
    @Transactional
    public void requestBlock(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new CardOwnershipException("Access denied: Card doesn't belong to user");
        }
        if (card.getCardStatus() == CardStatus.BLOCKED) {
            throw new CardStatusException("Card is already blocked");
        }
        log.info("User {} requested block for card {}", username, cardEncryptionService.getMaskedCardNumber(card.getCardNumber()));
        card.setCardStatus(CardStatus.PENDING_BLOCK);
    }

    /**
     * Retrieves a paginated list of bank cards owned by a specific user.
     * Card numbers in the response are masked.
     *
     * @param username The username of the user whose cards are to be retrieved.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link CardResponseDto} representing the user's cards.
     */
    public Page<CardResponseDto> getUserCards(String username, Pageable pageable) {
        Page<Card> cardsPage = cardRepository.findByUserUsernamePageable(username, pageable);
        return cardsPage.map(card -> {
            CardResponseDto dto = cardMapper.toCardResponseDto(card);
            dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(card.getCardNumber()));
            return dto;
        });
    }

    /**
     * Retrieves details of a specific bank card by its ID, ensuring it belongs to the specified user.
     * The card number in the response is masked.
     *
     * @param cardId The ID of the card to retrieve.
     * @param username The username of the user who should own the card.
     * @return A {@link CardResponseDto} representing the retrieved card.
     * @throws CardNotFoundException if no card is found with the given ID.
     * @throws CardOwnershipException if the card does not belong to the specified user.
     */
    public CardResponseDto getUserCardById(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        if(!card.getUser().getUsername().equals(username)) {
            throw new CardOwnershipException("Access denied: Card doesn't belong to user");
        }
        CardResponseDto dto = cardMapper.toCardResponseDto(card);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(card.getCardNumber()));
        return dto;
    }
}