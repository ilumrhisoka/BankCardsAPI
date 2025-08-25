package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Transfer;
import com.example.bankcards.service.card.CardEncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferDtoMapper {
    private final CardEncryptionService cardEncryptionService;

    public TransferResponseDto toTransferResponseDto(Transfer transfer, String username) {
        TransferResponseDto dto = new TransferResponseDto();
        dto.setId(transfer.getId());
        dto.setFromCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getFromCard().getCardNumber()));
        dto.setToCardNumber(cardEncryptionService.getMaskedCardNumber(transfer.getToCard().getCardNumber()));
        dto.setAmount(transfer.getAmount());
        dto.setStatus(transfer.getStatus());
        dto.setTransferDate(transfer.getTransferDate());
        dto.setCreatedAt(transfer.getCreatedAt());

        return dto;
    }
}