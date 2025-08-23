package com.example.bankcards.util;

import com.example.bankcards.dto.transfer.TransferResponseDto;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.service.CardEncryptionService;
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

        dto.setOutgoing(transfer.getFromCard().getUser().getUsername().equals(username));

        return dto;
    }
}
