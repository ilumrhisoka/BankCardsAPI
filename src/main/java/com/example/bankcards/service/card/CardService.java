package com.example.bankcards.service.card;

import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.card.CardCreateRequest;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.dto.card.CardUpdateRequest;
import com.example.bankcards.model.entity.Account;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.account.AccountService;
import com.example.bankcards.util.mapper.CardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AccountService accountService; // Injected AccountService

    /**
     * Creates a new bank card for a specified user.
     * The card number is encrypted before saving.
     *
     * @param request The {@link CardCreateRequest} containing details for the new card.
     * @return A {@link CardResponseDto} representing the newly created card with its number masked.
     * @throws UserNotFoundException if the user specified by {@code userId} in the request is not found.
     * @throws IllegalArgumentException if the card number in the request is invalid.
     * @throws RuntimeException if card number encryption fails.
     */
    @Transactional
    public CardResponseDto createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Find or create a default account for the user
        Account account = accountService.findOrCreateDefaultAccount(user);

        String encryptedCardNumber = cardEncryptionService.encryptCardNumber(request.getCardNumber());

        Card card = new Card();
        card.setCardNumber(encryptedCardNumber);
        card.setCardHolder(request.getCardHolder());
        card.setExpiryDate(request.getExpiryDate());
        card.setBalance(request.getBalance());
        card.setCardStatus(CardStatus.ACTIVE);
        card.setAccount(account); // Link card to account

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
     * @throws CardStatusException if the card is already blocked or pending block.
     */
    @Transactional
    public CardResponseDto blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getCardStatus() == CardStatus.BLOCKED || card.getCardStatus() == CardStatus.PENDING_BLOCK) {
            throw new CardStatusException("Card is already blocked or pending block.");
        }

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
     * @throws CardStatusException if the card is already active or pending unblock.
     */
    @Transactional
    public CardResponseDto activateCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getCardStatus() == CardStatus.ACTIVE || card.getCardStatus() == CardStatus.PENDING_UNBLOCK) {
            throw new CardStatusException("Card is already active or pending unblock.");
        }

        card.setCardStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Activated card with ID: {}", id);
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    /**
     * Approves a pending block request for a card, changing its status to BLOCKED.
     *
     * @param id The ID of the card to approve blocking for.
     * @return A {@link CardResponseDto} representing the blocked card.
     * @throws CardNotFoundException if no card is found with the given ID.
     * @throws CardStatusException if the card is not in PENDING_BLOCK state.
     */
    @Transactional
    public CardResponseDto approveBlockRequest(Long id) {
        log.info("Admin attempting to approve block request for card ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Card not found with ID: {}", id);
                    return new CardNotFoundException("Card not found");
                });

        if (card.getCardStatus() != CardStatus.PENDING_BLOCK) {
            log.warn("Cannot approve block request for card ID: {}. Current status is not PENDING_BLOCK: {}", id, card.getCardStatus());
            throw new CardStatusException("Card is not in PENDING_BLOCK status.");
        }

        card.setCardStatus(CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        log.info("Successfully approved block request for card ID: {}. New status: BLOCKED", id);
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    /**
     * Approves a pending unblock request for a card, changing its status to ACTIVE.
     *
     * @param id The ID of the card to approve unblocking for.
     * @return A {@link CardResponseDto} representing the activated card.
     * @throws CardNotFoundException if no card is found with the given ID.
     * @throws CardStatusException if the card is not in PENDING_UNBLOCK state.
     */
    @Transactional
    public CardResponseDto approveUnblockRequest(Long id) {
        log.info("Admin attempting to approve unblock request for card ID: {}", id);
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Card not found with ID: {}", id);
                    return new CardNotFoundException("Card not found");
                });

        if (card.getCardStatus() != CardStatus.PENDING_UNBLOCK) {
            log.warn("Cannot approve unblock request for card ID: {}. Current status is not PENDING_UNBLOCK: {}", id, card.getCardStatus());
            throw new CardStatusException("Card is not in PENDING_UNBLOCK status.");
        }

        card.setCardStatus(CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        log.info("Successfully approved unblock request for card ID: {}. New status: ACTIVE", id);
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    // ... (внутри CardService)

    @Transactional
    public CardResponseDto deposit(Long id, java.math.BigDecimal amount) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        card.setBalance(card.getBalance().add(amount));
        // Также обновляем баланс счета (аккаунта), так как карта привязана к нему
        card.getAccount().setBalance(card.getAccount().getBalance().add(amount));

        Card savedCard = cardRepository.save(card);
        log.info("Deposited {} to card {}", amount, id);

        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    @Transactional
    public CardResponseDto withdraw(Long id, java.math.BigDecimal amount) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (card.getBalance().compareTo(amount) < 0) {
            throw new com.example.bankcards.exception.card.InsufficientFundsException("Insufficient funds");
        }

        card.setBalance(card.getBalance().subtract(amount));
        card.getAccount().setBalance(card.getAccount().getBalance().subtract(amount));

        Card savedCard = cardRepository.save(card);
        log.info("Withdrew {} from card {}", amount, id);

        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }

    // ... внутри класса

    @Transactional
    public CardResponseDto declineRequest(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getCardStatus() == CardStatus.PENDING_BLOCK) {
            // Отклоняем блокировку -> возвращаем в ACTIVE
            card.setCardStatus(CardStatus.ACTIVE);
            log.info("Block request declined for card {}", id);
        } else if (card.getCardStatus() == CardStatus.PENDING_UNBLOCK) {
            // Отклоняем разблокировку -> возвращаем в BLOCKED
            card.setCardStatus(CardStatus.BLOCKED);
            log.info("Unblock request declined for card {}", id);
        } else {
            throw new CardStatusException("Card has no pending requests.");
        }

        Card savedCard = cardRepository.save(card);
        CardResponseDto dto = cardMapper.toCardResponseDto(savedCard);
        dto.setCardNumber(cardEncryptionService.getMaskedCardNumber(savedCard.getCardNumber()));
        return dto;
    }
}