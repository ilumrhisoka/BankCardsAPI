package com.example.bankcards.service.transfer;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardOwnershipException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.dto.BadRequestException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.exception.transfer.InvalidTransferException;
import com.example.bankcards.model.dto.transfer.TransferRequest;
import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Account;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.Transfer;
import com.example.bankcards.model.entity.Transaction;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.NotificationType;
import com.example.bankcards.model.entity.enums.TransferStatus;
import com.example.bankcards.model.entity.enums.TransactionType;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.service.card.CardEncryptionService;
import com.example.bankcards.service.notification.NotificationService;
import com.example.bankcards.util.mapper.TransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransferService {

    private final CardEncryptionService cardEncryptionService;
    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final TransferMapper transferMapper;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Transactional
    public TransferResponseDto createTransfer(TransferRequest request, String username) {
        Card fromCard = getAndValidateFromCard(request.getFromCardId(), username);

        Card toCard;
        // Логика выбора получателя: ID или Номер
        if (request.getToCardId() != null) {
            toCard = cardRepository.findById(request.getToCardId())
                    .orElseThrow(() -> new CardNotFoundException("Destination card not found with ID: " + request.getToCardId()));
            // Проверяем статус, даже если по ID нашли
            if (toCard.getCardStatus() != CardStatus.ACTIVE) {
                throw new CardStatusException("Destination card is not active.");
            }
        } else if (request.getToCardNumber() != null && !request.getToCardNumber().trim().isEmpty()) {
            toCard = getAndValidateToCard(request.getToCardNumber(), fromCard);
        } else {
            throw new BadRequestException("Destination card info (ID or Number) is missing");
        }

        if (fromCard.getId().equals(toCard.getId())) {
            throw new InvalidTransferException("Cannot transfer to the same card");
        }

        validateTransferConditions(fromCard, request.getAmount());

        Account fromAccount = fromCard.getAccount();
        Account toAccount = toCard.getAccount();

        Transfer transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(request.getAmount());
        transfer.setTransferDate(LocalDateTime.now());

        try {
            // 1. Балансы
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            // 2. Счета
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
            toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            // 3. Транзакции
            recordTransaction(fromAccount, fromCard, request.getAmount().negate(), TransactionType.TRANSFER_OUT,
                    "Transfer to card ID " + toCard.getId());
            recordTransaction(toAccount, toCard, request.getAmount(), TransactionType.TRANSFER_IN,
                    "Transfer from card ID " + fromCard.getId());

            // 4. Сохранение перевода
            transfer.setStatus(TransferStatus.SUCCESS);
            Transfer savedTransfer = transferRepository.save(transfer);

            // 5. Уведомления (Колокольчик)
            notificationService.createNotification(
                    fromAccount.getUser().getUsername(),
                    "Списание: -" + request.getAmount() + " (Перевод)",
                    NotificationType.INFO
            );
            notificationService.createNotification(
                    toAccount.getUser().getUsername(),
                    "Пополнение: +" + request.getAmount() + " (Входящий перевод)",
                    NotificationType.SUCCESS
            );

            log.info("Transfer completed: {} -> {} amount: {}", fromCard.getId(), toCard.getId(), request.getAmount());

            return mapTransferToDto(savedTransfer);

        } catch (Exception e) {
            log.error("Transfer failed", e);
            transfer.setStatus(TransferStatus.FAILED);
            transferRepository.save(transfer);
            throw new RuntimeException("Transfer processing failed", e);
        }
    }

    private void recordTransaction(Account account, Card card, BigDecimal amount, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCard(card);
        transaction.setAmount(amount.abs());
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setDescription(description);
        transaction.setStatus(TransferStatus.SUCCESS);
        transactionRepository.save(transaction);
    }

    private Card getAndValidateFromCard(Long fromCardId, String username) {
        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException("Source card not found with ID: " + fromCardId));

        if (!fromCard.getAccount().getUser().getUsername().equals(username)) {
            throw new CardOwnershipException("Access denied: Source card doesn't belong to user");
        }
        if (fromCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardStatusException("Source card is not active.");
        }
        return fromCard;
    }

    private Card getAndValidateToCard(String toCardNumber, Card fromCard) {
        Card toCard = cardRepository.findAll().stream()
                .filter(card -> cardEncryptionService.matchesCardNumber(toCardNumber, card.getCardNumber()))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Destination card not found"));

        if (toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardStatusException("Destination card is not active.");
        }
        return toCard;
    }

    private void validateTransferConditions(Card fromCard, BigDecimal amount) {
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds.");
        }
    }

    private TransferResponseDto mapTransferToDto(Transfer transfer) {
        TransferResponseDto dto = transferMapper.toTransferResponseDto(transfer);
        if (transfer.getFromCard() != null) {
            dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getFromCard().getCardNumber()));
        }
        if (transfer.getToCard() != null) {
            dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getToCard().getCardNumber()));
        }
        return dto;
    }

    public List<TransferResponseDto> getUserTransfers(String username) {
        List<Transfer> transfers = transferRepository.findByUserUsername(username);
        return transfers.stream().map(this::mapTransferToDto).collect(Collectors.toList());
    }

    public List<TransferResponseDto> getCardTransfers(Long cardId, String username) {
        Card card = cardRepository.findById(cardId).orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (!card.getAccount().getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied");
        }
        return transferRepository.findByCardId(cardId).stream().map(this::mapTransferToDto).collect(Collectors.toList());
    }

    public TransferResponseDto getTransfer(Long transferId, String username) {
        Transfer transfer = transferRepository.findById(transferId).orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        boolean isParticipant = transfer.getFromCard().getAccount().getUser().getUsername().equals(username) ||
                transfer.getToCard().getAccount().getUser().getUsername().equals(username);
        if (!isParticipant) throw new ForbiddenException("Access denied");
        return mapTransferToDto(transfer);
    }
}