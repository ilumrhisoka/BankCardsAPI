package com.example.bankcards.service.transfer;

import com.example.bankcards.model.dto.transfer.TransferRequest;
import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.Transfer;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.TransferStatus;
import com.example.bankcards.exception.dto.BadRequestException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.card.CardEncryptionService;
// import com.example.bankcards.util.CardMaskingUtil; // Удаляем, используем CardEncryptionService
import com.example.bankcards.util.mapper.TransferMapper; // Используем TransferMapper
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public TransferResponseDto createTransfer(TransferRequest request, String username) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException("Source card not found with ID: " + request.getFromCardId()));

        if (!fromCard.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Card doesn't belong to user");
        }
        if (fromCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException("Source card is not active. Current status: " + fromCard.getCardStatus());
        }

        Card toCard = findCardByNumber(request.getToCardNumber());

        if (!toCard.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Target card doesn't belong to the same user");
        }

        if (toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException("Destination card is not active. Current status: " + toCard.getCardStatus());
        }
        if (fromCard.getId().equals(toCard.getId())) {
            throw new BadRequestException("Cannot transfer to the same card");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds on source card. Available: " + fromCard.getBalance() + ", Requested: " + request.getAmount());
        }

        try {
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            Transfer transfer = new Transfer();
            transfer.setFromCard(fromCard);
            transfer.setToCard(toCard);
            transfer.setAmount(request.getAmount());
            transfer.setTransferDate(LocalDateTime.now());
            transfer.setStatus(TransferStatus.SUCCESS);

            Transfer savedTransfer = transferRepository.save(transfer);

            log.info("Transfer completed: {} -> {} amount: {}",
                    cardEncryptionService.getMaskedCardNumber(fromCard.getCardNumber()),
                    cardEncryptionService.getMaskedCardNumber(toCard.getCardNumber()),
                    request.getAmount());

            TransferResponseDto dto = transferMapper.toTransferResponseDto(savedTransfer);
            dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(savedTransfer.getFromCard().getCardNumber()));
            dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(savedTransfer.getToCard().getCardNumber()));
            return dto;

        } catch (Exception e) {
            log.error("Transfer failed due to: {}", e.getMessage());

            Transfer failedTransfer = new Transfer();
            failedTransfer.setFromCard(fromCard);
            failedTransfer.setAmount(request.getAmount());
            failedTransfer.setTransferDate(LocalDateTime.now());
            failedTransfer.setStatus(TransferStatus.FAILED);

            Transfer savedFailedTransfer = transferRepository.save(failedTransfer);

            throw new BadRequestException("Transfer failed: " + e.getMessage());
        }
    }

    public List<TransferResponseDto> getUserTransfers(String username) {
        List<Transfer> transfers = transferRepository.findByUserUsername(username);
        return transfers.stream()
                .map(transfer -> {
                    TransferResponseDto dto = transferMapper.toTransferResponseDto(transfer);
                    dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getFromCard().getCardNumber()));
                    dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getToCard().getCardNumber()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TransferResponseDto> getCardTransfers(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new ForbiddenException("Access denied: Card doesn't belong to user");
        }

        List<Transfer> transfers = transferRepository.findByCardId(cardId);
        return transfers.stream()
                .map(transfer -> {
                    TransferResponseDto dto = transferMapper.toTransferResponseDto(transfer);
                    dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getFromCard().getCardNumber()));
                    dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getToCard().getCardNumber()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public TransferResponseDto getTransfer(Long transferId, String username) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));

        boolean isParticipant = transfer.getFromCard().getUser().getUsername().equals(username) ||
                transfer.getToCard().getUser().getUsername().equals(username);

        if (!isParticipant) {
            throw new ForbiddenException("Access denied: User " + username + " is not participant of this transfer");
        }

        TransferResponseDto dto = transferMapper.toTransferResponseDto(transfer);
        dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getFromCard().getCardNumber()));
        dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getToCard().getCardNumber()));
        return dto;
    }

    private Card findCardByNumber(String plainCardNumber) {
        return cardRepository.findAll().stream()
                .filter(card -> cardEncryptionService.matchesCardNumber(plainCardNumber, card.getCardNumber()))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }
}