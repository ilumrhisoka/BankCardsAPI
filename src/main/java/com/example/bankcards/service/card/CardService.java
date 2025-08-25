package com.example.bankcards.service.card;

import com.example.bankcards.model.dto.card.CardCreateRequest;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.dto.card.CardUpdateRequest;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.CardMapper; // Используем CardMapper
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing bank card operations.
 * This service handles the creation, retrieval, updating, deletion,
 * blocking, and activation of bank cards. It interacts with {@link CardRepository},
 * {@link UserRepository}, and {@link CardEncryptionService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardEncryptionService cardEncryptionService;
    private final CardMapper cardMapper;

    /**
     * Creates a new bank card for a specified user.
     * The card number is encrypted before saving.
     *
     * @param request The {@link CardCreateRequest} containing details for the new card.
     * @return A {@link CardResponseDto} representing the newly created card with its number masked.
     * @throws UsernameNotFoundException if the user specified by {@code userId} in the request is not found.
     * @throws IllegalArgumentException if the card number in the request is invalid.
     * @throws RuntimeException if card number encryption fails.
     */
    @Transactional
    public CardResponseDto createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

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
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    /**
     * Retrieves a paginated list of all bank cards.
     * Card numbers in the response are masked.
     *
     * @param pageable The pagination information.
     * @return A {@link Page} of {@link CardResponseDto} representing all cards.
     */
    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);
        return cards.map(card -> {
            CardResponseDto dto = cardMapper.toCardResponseDto(card);
            dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(card.getCardNumber()));
            return dto;
        });
    }

    /**
     * Retrieves details of a specific bank card by its ID.
     * The card number in the response is masked.
     *
     * @param id The ID of the card to retrieve.
     * @return A {@link CardResponseDto} representing the retrieved card with its number masked.
     * @throws CardNotFoundException if no card is found with the given ID.
     */
    public CardResponseDto getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        CardResponseDto dto = cardMapper.toCardResponseDto(card);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(card.getCardNumber()));
        return dto;
    }

    /**
     * Updates details of an existing bank card.
     * Only provided fields in the request will be updated.
     * The card number in the response is masked.
     *
     * @param id The ID of the card to update.
     * @param request The {@link CardUpdateRequest} containing the fields to update.
     * @return A {@link CardResponseDto} representing the updated card with its number masked.
     * @throws CardNotFoundException if no card is found with the given ID.
     */
    @Transactional
    public CardResponseDto updateCard(Long id, CardUpdateRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

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
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    /**
     * Deletes a bank card permanently by its ID.
     *
     * @param id The ID of the card to delete.
     * @throws CardNotFoundException if no card is found with the given ID.
     */
    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        cardRepository.delete(card);
        log.info("Deleted card with ID: {}", id);
    }

    /**
     * Blocks a specific bank card by changing its status to {@code BLOCKED}.
     * The card number in the response is masked.
     *
     * @param id The ID of the card to block.
     * @return A {@link CardResponseDto} representing the blocked card with its number masked.
     * @throws CardNotFoundException if no card is found with the given ID.
     */
    @Transactional
    public CardResponseDto blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setCardStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        log.info("Blocked card with ID: {}", id);
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    /**
     * Activates a previously blocked or inactive bank card by changing its status to {@code ACTIVE}.
     * The card number in the response is masked.
     *
     * @param id The ID of the card to activate.
     * @return A {@link CardResponseDto} representing the activated card with its number masked.
     * @throws CardNotFoundException if no card is found with the given ID.
     */
    @Transactional
    public CardResponseDto activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setCardStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Activated card with ID: {}", id);
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }
}