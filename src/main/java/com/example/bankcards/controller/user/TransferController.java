package com.example.bankcards.controller.user;

import com.example.bankcards.dto.transfer.TransferRequest;
import com.example.bankcards.dto.transfer.TransferResponseDto;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user/transfers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponseDto> createTransfer(@Valid @RequestBody TransferRequest request,
                                                              Authentication authentication) {
        String username = authentication.getName();
        TransferResponseDto transfer = transferService.createTransfer(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    @GetMapping("/my")
    public ResponseEntity<List<TransferResponseDto>> getMyTransfers(Authentication authentication) {
        String username = authentication.getName();
        List<TransferResponseDto> transfers = transferService.getUserTransfers(username);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/card/{cardId}")
    public ResponseEntity<List<TransferResponseDto>> getCardTransfers(@PathVariable Long cardId,
                                                                      Authentication authentication) {
        String username = authentication.getName();
        List<TransferResponseDto> transfers = transferService.getCardTransfers(cardId, username);
        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponseDto> getTransfer(@PathVariable Long transferId,
                                                           Authentication authentication) {
        String username = authentication.getName();
        TransferResponseDto transfer = transferService.getTransfer(transferId, username);
        return ResponseEntity.ok(transfer);
    }
}