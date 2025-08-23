package com.example.bankcards.controller.user;

import com.example.bankcards.dto.card.CardResponseDto;
import com.example.bankcards.dto.transfer.TransferResponseDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.UserCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
public class UserCardController {
    private final UserCardService userCardService;

    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getUserCards(Authentication authentication, Pageable pageable) {
        String username = authentication.getName();
        Page<CardResponseDto> cards = userCardService.getUserCards(username, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable Long cardId,
                                                       Authentication authentication) {
        String username = authentication.getName();
        CardResponseDto card = userCardService.getUserCardById(cardId, username);
        return ResponseEntity.ok(card);
    }

    @PostMapping("/block/{cardId}")
    public ResponseEntity<?> requestBlockCard(@PathVariable Long cardId,
                                              Authentication authentication) {
        String username = authentication.getName();
        userCardService.requestBlock(cardId, username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unblock/{cardId}")
    public ResponseEntity<?> requestUnblockCard(@PathVariable Long cardId,
                                              Authentication authentication) {
        String username = authentication.getName();
        userCardService.requestUnblock(cardId, username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(Authentication authentication) {
        String username = authentication.getName();
        BigDecimal balance = userCardService.getTotalBalance(username);
        return ResponseEntity.ok(balance);
    }
}
