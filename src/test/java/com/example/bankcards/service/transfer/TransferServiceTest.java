package com.example.bankcards.service.transfer;

import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.dto.BadRequestException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.model.dto.transfer.TransferRequest;
import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.card.CardEncryptionService;
import com.example.bankcards.util.mapper.TransferDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private TransferRepository transferRepository;
    @Mock
    private CardEncryptionService cardEncryptionService;
    @Mock
    private TransferDtoMapper transferDtoMapper;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "pass", Role.ROLE_USER);
        testUser.setId(1L);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setCardNumber("encrypted_1234");
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setCardStatus(CardStatus.ACTIVE);
        fromCard.setUser(testUser);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setCardNumber("encrypted_5678");
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setCardStatus(CardStatus.ACTIVE);
        toCard.setUser(testUser);

        transferRequest = new TransferRequest();

        lenient().when(cardEncryptionService.matchesCardNumber(anyString(), eq("encrypted_1234"))).thenReturn(false);
        lenient().when(cardEncryptionService.matchesCardNumber(anyString(), eq("encrypted_5678"))).thenReturn(false);
    }

    @Test
    void createTransfer_whenValid_shouldSucceed() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCard));
        when(cardEncryptionService.matchesCardNumber("plain_5678", "encrypted_5678")).thenReturn(true);

        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transferDtoMapper.toTransferResponseDto(any(), anyString())).thenReturn(new TransferResponseDto());

        TransferResponseDto result = transferService.createTransfer(transferRequest, "testuser");

        assertNotNull(result);
        assertEquals(new BigDecimal("900.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("600.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository).save(any());
    }

    @Test
    void createTransfer_whenInsufficientFunds_shouldThrowInsufficientFundsException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        fromCard.setBalance(new BigDecimal("50.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCard));
        when(cardEncryptionService.matchesCardNumber("plain_5678", "encrypted_5678")).thenReturn(true);

        assertThrows(InsufficientFundsException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verify(cardRepository, never()).save(any());
    }

    @Test
    void createTransfer_whenFromCardBlocked_shouldThrowCardBlockedException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        fromCard.setCardStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(CardBlockedException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
    }

    @Test
    void createTransfer_toSameCard_shouldThrowBadRequestException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_1234");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCard));
        when(cardEncryptionService.matchesCardNumber("plain_1234", "encrypted_1234")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
    }

    @Test
    void createTransfer_whenUserNotOwnerOfFromCard_shouldThrowForbiddenException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(ForbiddenException.class, () -> {
            transferService.createTransfer(transferRequest, "wronguser");
        });
    }
}