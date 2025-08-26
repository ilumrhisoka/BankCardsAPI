package com.example.bankcards.service.transfer;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardOwnershipException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.exception.transfer.InvalidTransferException;
import com.example.bankcards.model.dto.transfer.TransferRequest;
import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.Transfer;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.TransferStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.card.CardEncryptionService;
import com.example.bankcards.util.mapper.TransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing money transfer operations.
 * This service handles the creation and retrieval of transfers, including validation
 * of card status, ownership, and sufficient funds.
 *
 * <p>Annotated with {@link org.springframework.stereotype.Service @Service} to indicate
 * its role as a service component in the Spring application context.</p>
 *
 * <p>Uses {@link lombok.RequiredArgsConstructor @RequiredArgsConstructor} for constructor
 * injection of dependencies and {@link lombok.extern.slf4j.Slf4j @Slf4j} for logging.</p>
 *
 * <p>All methods within this class are {@link org.springframework.transaction.annotation.Transactional @Transactional}
 * with {@code readOnly = true} by default, meaning that read operations do not require
 * a new transaction unless explicitly overridden.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransferService {

    private final CardEncryptionService cardEncryptionService;
    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final TransferMapper transferMapper;

    /**
     * Initiates a new money transfer between two cards.
     * Performs various validations: card existence, ownership, status, sufficient funds,
     * and prevents transfer to the same card.
     *
     * @param request The {@link TransferRequest} containing transfer details.
     * @param username The username of the authenticated user attempting the transfer.
     * @return A {@link TransferResponseDto} representing the created transfer.
     * @throws CardNotFoundException      if the source or destination card is not found.
     * @throws CardOwnershipException     if the source card does not belong to the authenticated user.
     * @throws CardStatusException        if the source or destination card is not active.
     * @throws InvalidTransferException   if attempting to transfer to the same card.
     * @throws InsufficientFundsException if the source card has insufficient funds.
     * @throws ForbiddenException         if the target card does not belong to the same user.
     */
    @Transactional
    public TransferResponseDto createTransfer(TransferRequest request, String username) {
        Card fromCard = getAndValidateFromCard(request.getFromCardId(), username);

        Card toCard = getAndValidateToCard(request.getToCardNumber(), fromCard, username);

        validateTransferConditions(fromCard, request.getAmount());

        Transfer transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(request.getAmount());
        transfer.setTransferDate(LocalDateTime.now());

        try {
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            transfer.setStatus(TransferStatus.SUCCESS);
            Transfer savedTransfer = transferRepository.save(transfer);

            log.info("Transfer completed: {} -> {} amount: {}",
                    cardEncryptionService.getMaskedCardNumber(fromCard.getCardNumber()),
                    cardEncryptionService.getMaskedCardNumber(toCard.getCardNumber()),
                    request.getAmount());

            return mapTransferToDto(savedTransfer);

        } catch (Exception e) {
            log.error("Transfer failed due to an unexpected error during processing: {}", e.getMessage(), e);
            transfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(transfer);
            throw new RuntimeException("An unexpected error occurred during transfer processing.", e);
        }
    }

    /**
     * Retrieves and validates the source card for a transfer.
     * Checks if the card exists, belongs to the specified user, and is active.
     *
     * @param fromCardId The ID of the source card.
     * @param username The username of the card owner.
     * @return The validated {@link Card} object representing the source card.
     * @throws CardNotFoundException if the card with the given ID is not found.
     * @throws CardOwnershipException if the card does not belong to the specified user.
     * @throws CardStatusException if the card is not in an {@code ACTIVE} status.
     */
    private Card getAndValidateFromCard(Long fromCardId, String username) {
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found with ID: " + fromCardId));

        if (!fromCard.getUser().getUsername().equals(username)) {
            throw new CardOwnershipException("Access denied: Source card doesn't belong to user");
        }
        if (fromCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardStatusException("Source card is not active. Current status: " + fromCard.getCardStatus());
        }
        return fromCard;
    }

    /**
     * Retrieves and validates the destination card for a transfer.
     * Checks if the card exists, belongs to the specified user, is active,
     * and is not the same as the source card.
     *
     * @param toCardNumber The card number of the destination card.
     * @param fromCard The source card from which the transfer originates.
     * @param username The username of the card owner.
     * @return The validated {@link Card} object representing the destination card.
     * @throws CardNotFoundException if the destination card with the given number is not found.
     * @throws ForbiddenException if the target card does not belong to the same user.
     * @throws CardStatusException if the destination card is not in an {@code ACTIVE} status.
     * @throws InvalidTransferException if the source and destination cards are the same.
     */
    private Card getAndValidateToCard(String toCardNumber, Card fromCard, String username) {
        Card toCard = cardRepository.findByUserUsername(username).stream()
                .filter(card -> cardEncryptionService.matchesCardNumber(toCardNumber, card.getCardNumber()))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Destination card not found with number: " + toCardNumber));

        // Проверка владения картой назначения
        if (!toCard.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Target card doesn't belong to the same user");
        }

        if (toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardStatusException("Destination card is not active. Current status: " + toCard.getCardStatus());
        }
        if (fromCard.getId().equals(toCard.getId())) {
            throw new InvalidTransferException("Cannot transfer to the same card");
        }
        return toCard;
    }

    /**
     * Validates transfer conditions, specifically checking for sufficient funds on the source card.
     *
     * @param fromCard The source card.
     * @param amount The amount to be transferred.
     * @throws InsufficientFundsException if the source card has insufficient funds.
     */
    private void validateTransferConditions(Card fromCard, BigDecimal amount) {
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card. Available: " + fromCard.getBalance() + ", Requested: " + amount);
        }
    }

    /**
     * Maps a {@link Transfer} entity to a {@link TransferResponseDto},
     * masking the card numbers for security.
     *
     * @param transfer The {@link Transfer} entity to map.
     * @return A {@link TransferResponseDto} with masked card numbers.
     */
    private TransferResponseDto mapTransferToDto(Transfer transfer) {
        TransferResponseDto dto = transferMapper.toTransferResponseDto(transfer);
        if (transfer.getFromCard() != null) {
            dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getFromCard().getCardNumber()));
        } else {
            dto.setFromCardNumber("N/A");
        }
        if (transfer.getToCard() != null) {
            dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getToCard().getCardNumber()));
        } else {
            dto.setToCardNumber("N/A");
        }
        return dto;
    }

    /**
     * Retrieves a list of all money transfers associated with a given user (either as sender or receiver).
     *
     * @param username The username of the user whose transfers are to be retrieved.
     * @return A {@link List} of {@link TransferResponseDto} representing the user's transfers.
     */
    public List<TransferResponseDto> getUserTransfers(String username) {
        List<Transfer> transfers = transferRepository.findByUserUsername(username);
        return transfers.stream()
                .map(this::mapTransferToDto) // Используем новый вспомогательный метод
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all money transfers associated with a specific card ID.
     * Ensures that the card belongs to the authenticated user.
     *
     * @param cardId The ID of the card for which to retrieve transfers.
     * @param username The username of the authenticated user.
     * @return A {@link List} of {@link TransferResponseDto} representing the card's transfers.
     * @throws CardNotFoundException  if the card is not found.
     * @throws ForbiddenException if the card does not belong to the authenticated user.
     */
    public List<TransferResponseDto> getCardTransfers(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Card doesn't belong to user");
        }

        List<Transfer> transfers = transferRepository.findByCardId(cardId);
        return transfers.stream()
                .map(this::mapTransferToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves details of a specific transfer by its ID.
     * Ensures that the transfer is associated with the authenticated user (either as sender or receiver).
     *
     * @param transferId The ID of the transfer to retrieve.
     * @param username The username of the authenticated user.
     * @return A {@link TransferResponseDto} representing the retrieved transfer.
     * @throws ResourceNotFoundException if the transfer is not found.
     * @throws ForbiddenException        if the user is not a participant of the transfer.
     */
    public TransferResponseDto getTransfer(Long transferId, String username) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        boolean isParticipant = transfer.getFromCard().getUser().getUsername().equals(username) ||
                transfer.getToCard().getUser().getUsername().equals(username);
        if (!isParticipant) {
            throw new ForbiddenException("Access denied: User " + username + " is not participant of this transfer");
        }
        return mapTransferToDto(transfer);
    }
}