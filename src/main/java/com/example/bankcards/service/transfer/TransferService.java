package com.example.bankcards.service.transfer;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransferStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.card.CardEncryptionService;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.mapper.TransferDtoMapper;
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
    private final TransferDtoMapper transferDtoMapper;

    @Transactional
    public TransferResponseDto createTransfer(TransferRequest request, String username) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new RuntimeException("From card not found"));


        if (!fromCard.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied: Card doesn't belong to user");
        }
        if (fromCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("From card is not active");
        }
        Card toCard = findCardByNumber(request.getToCardNumber());

        if (toCard == null) {
            throw new RuntimeException("To card not found");
        }
        if (!toCard.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied: Target card doesn't belong to the same user");
        }
        if (toCard.getCardStatus() != CardStatus.ACTIVE) {
            throw new RuntimeException("To card is not active");
        }
        if (fromCard.getId().equals(toCard.getId())) {
            throw new RuntimeException("Cannot transfer to the same card");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
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
                    CardMaskingUtil.maskCardNumber(fromCard.getCardNumber()),
                    CardMaskingUtil.maskCardNumber(toCard.getCardNumber()),
                    request.getAmount());

            return transferDtoMapper.toTransferResponseDto(savedTransfer, username);

        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());

            Transfer failedTransfer = new Transfer();
            failedTransfer.setFromCard(fromCard);
            failedTransfer.setToCard(toCard);
            failedTransfer.setAmount(request.getAmount());
            failedTransfer.setTransferDate(LocalDateTime.now());
            failedTransfer.setStatus(TransferStatus.FAILED);

            Transfer savedTransfer = transferRepository.save(failedTransfer);

            throw new RuntimeException("Transfer failed: " + e.getMessage());
        }
    }

    public List<TransferResponseDto> getUserTransfers(String username) {
        List<Transfer> transfers = transferRepository.findByUserUsername(username);
        return transfers.stream()
                .map(transfer -> transferDtoMapper.toTransferResponseDto(transfer, username))
                .collect(Collectors.toList());
    }

    public List<TransferResponseDto> getCardTransfers(Long cardId, String username) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied: Card doesn't belong to user");
        }

        List<Transfer> transfers = transferRepository.findByCardId(cardId);
        return transfers.stream()
                .map(transfer -> transferDtoMapper.toTransferResponseDto(transfer, username))
                .collect(Collectors.toList());
    }

    public TransferResponseDto getTransfer(Long transferId, String username) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        boolean isParticipant = transfer.getFromCard().getUser().getUsername().equals(username) ||
                transfer.getToCard().getUser().getUsername().equals(username);

        if (!isParticipant) {
            throw new RuntimeException("Access denied: User is not participant of this transfer");
        }

        return transferDtoMapper.toTransferResponseDto(transfer, username);
    }

    private Card findCardByNumber(String plainCardNumber) {
        return cardRepository.findAll().stream()
                .filter(card -> cardEncryptionService.matchesCardNumber(plainCardNumber, card.getCardNumber()))
                .findFirst()
                .orElse(null);
    }
}
