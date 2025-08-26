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
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.model.entity.enums.TransferStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.card.CardEncryptionService;
import com.example.bankcards.util.mapper.TransferMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private TransferMapper transferMapper;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private User anotherUser;
    private Card fromCard;
    private Card toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "pass", Role.ROLE_USER);
        testUser.setId(1L);

        anotherUser = new User("anotheruser", "another@example.com", "pass", Role.ROLE_USER);
        anotherUser.setId(2L);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setCardNumber("encrypted_1234");
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setCardHolder("Test User");
        fromCard.setExpiryDate(LocalDateTime.now().toLocalDate().plusYears(1));
        fromCard.setCardStatus(CardStatus.ACTIVE);
        fromCard.setUser(testUser);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setCardNumber("encrypted_5678");
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setCardHolder("Test User");
        toCard.setExpiryDate(LocalDateTime.now().toLocalDate().plusYears(1));
        toCard.setCardStatus(CardStatus.ACTIVE);
        toCard.setUser(testUser);

        transferRequest = new TransferRequest();

        lenient().when(cardEncryptionService.matchesCardNumber("plain_1234", "encrypted_1234")).thenReturn(true);
        lenient().when(cardEncryptionService.matchesCardNumber("plain_5678", "encrypted_5678")).thenReturn(true);
        lenient().when(cardEncryptionService.matchesCardNumber("plain_9999", "encrypted_9999")).thenReturn(true);
    }

    @Test
    void createTransfer_whenValid_shouldSucceed() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        TransferResponseDto mapperReturnDto = new TransferResponseDto();
        mapperReturnDto.setFromCardNumber("masked_1234");
        mapperReturnDto.setToCardNumber("masked_5678");
        mapperReturnDto.setAmount(transferRequest.getAmount());
        mapperReturnDto.setStatus(TransferStatus.SUCCESS);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByUserUsername("testuser")).thenReturn(List.of(fromCard, toCard));

        when(cardEncryptionService.getMaskedCardNumber("encrypted_1234")).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber("encrypted_5678")).thenReturn("masked_5678");

        when(transferRepository.save(any(Transfer.class))).thenAnswer(inv -> {
            Transfer savedTransfer = inv.getArgument(0);
            savedTransfer.setId(1L);
            savedTransfer.setFromCard(fromCard);
            savedTransfer.setToCard(toCard);
            return savedTransfer;
        });
        when(transferMapper.toTransferResponseDto(any(Transfer.class))).thenReturn(mapperReturnDto);

        TransferResponseDto result = transferService.createTransfer(transferRequest, "testuser");

        assertNotNull(result);
        assertEquals(new BigDecimal("900.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("600.00"), toCard.getBalance());
        assertEquals("masked_1234", result.getFromCardNumber());
        assertEquals("masked_5678", result.getToCardNumber());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void createTransfer_whenInsufficientFunds_shouldThrowInsufficientFundsException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("1100.00"));

        fromCard.setBalance(new BigDecimal("1000.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByUserUsername("testuser")).thenReturn(List.of(fromCard, toCard));

        assertThrows(InsufficientFundsException.class, () -> transferService.createTransfer(transferRequest, "testuser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void createTransfer_whenFromCardBlocked_shouldThrowCardStatusException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        fromCard.setCardStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(CardStatusException.class, () -> transferService.createTransfer(transferRequest, "testuser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_toSameCard_shouldThrowInvalidTransferException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_1234");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByUserUsername("testuser")).thenReturn(List.of(fromCard, toCard));

        assertThrows(InvalidTransferException.class, () -> transferService.createTransfer(transferRequest, "testuser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenUserNotOwnerOfFromCard_shouldThrowCardOwnershipException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));


        assertThrows(CardOwnershipException.class, () -> transferService.createTransfer(transferRequest, "wronguser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenToCardNotFound_shouldThrowCardNotFoundException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("nonexistent_card_number");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByUserUsername("testuser")).thenReturn(Collections.singletonList(fromCard));

        assertThrows(CardNotFoundException.class, () -> transferService.createTransfer(transferRequest, "testuser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenToCardDoesNotBelongToUser_shouldThrowCardNotFoundException() {
        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setUser(anotherUser);
        cardOfAnotherUser.setCardNumber("encrypted_9999");

        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_9999");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByUserUsername("testuser")).thenReturn(List.of(fromCard, toCard));

        assertThrows(CardNotFoundException.class, () -> transferService.createTransfer(transferRequest, "testuser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenToCardBlocked_shouldThrowCardStatusException() {
        toCard.setCardStatus(CardStatus.BLOCKED);

        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        when(cardRepository.findByUserUsername("testuser")).thenReturn(List.of(fromCard, toCard));

        assertThrows(CardStatusException.class, () -> transferService.createTransfer(transferRequest, "testuser"));
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }


    @Test
    void getUserTransfers_shouldReturnListOfTransferResponseDto() {
        Transfer testTransfer = new Transfer();
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);

        TransferResponseDto dto = new TransferResponseDto();
        dto.setFromCardNumber("masked_1234");
        dto.setToCardNumber("masked_5678");

        when(transferRepository.findByUserUsername("testuser")).thenReturn(Collections.singletonList(testTransfer));
        when(transferMapper.toTransferResponseDto(testTransfer)).thenReturn(dto);
        when(cardEncryptionService.getMaskedCardNumber(fromCard.getCardNumber())).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber(toCard.getCardNumber())).thenReturn("masked_5678");

        List<TransferResponseDto> result = transferService.getUserTransfers("testuser");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("masked_1234", result.getFirst().getFromCardNumber());
        assertEquals("masked_5678", result.getFirst().getToCardNumber());
        verify(transferRepository).findByUserUsername("testuser");
    }

    @Test
    void getCardTransfers_shouldReturnListOfTransferResponseDto() {
        Transfer testTransfer = new Transfer();
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);

        TransferResponseDto dto = new TransferResponseDto();
        dto.setFromCardNumber("masked_1234");
        dto.setToCardNumber("masked_5678");

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(transferRepository.findByCardId(1L)).thenReturn(Collections.singletonList(testTransfer));
        when(transferMapper.toTransferResponseDto(testTransfer)).thenReturn(dto);
        when(cardEncryptionService.getMaskedCardNumber(fromCard.getCardNumber())).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber(toCard.getCardNumber())).thenReturn("masked_5678");

        List<TransferResponseDto> result = transferService.getCardTransfers(1L, "testuser");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(transferRepository).findByCardId(1L);
    }

    @Test
    void getCardTransfers_whenCardNotFound_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.getCardTransfers(99L, "testuser"));
        verifyNoInteractions(transferRepository, transferMapper);
    }

    @Test
    void getCardTransfers_whenCardDoesNotBelongToUser_shouldThrowForbiddenException() {
        fromCard.setUser(anotherUser);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(ForbiddenException.class, () -> transferService.getCardTransfers(1L, "testuser"));
        verifyNoInteractions(transferRepository, transferMapper);
    }

    @Test
    void getTransfer_shouldReturnTransferResponseDto() {
        Transfer testTransfer = new Transfer();
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);

        TransferResponseDto dto = new TransferResponseDto();
        dto.setFromCardNumber("masked_1234");
        dto.setToCardNumber("masked_5678");

        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));
        when(transferMapper.toTransferResponseDto(testTransfer)).thenReturn(dto);
        when(cardEncryptionService.getMaskedCardNumber(fromCard.getCardNumber())).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber(toCard.getCardNumber())).thenReturn("masked_5678");

        TransferResponseDto result = transferService.getTransfer(1L, "testuser");

        assertNotNull(result);
        assertEquals("masked_1234", result.getFromCardNumber());
        verify(transferRepository).findById(1L);
    }

    @Test
    void getTransfer_whenTransferNotFound_shouldThrowResourceNotFoundException() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transferService.getTransfer(99L, "testuser"));
    }

    @Test
    void getTransfer_whenUserNotParticipant_shouldThrowForbiddenException() {
        Transfer testTransfer = new Transfer();
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));

        assertThrows(ForbiddenException.class, () -> transferService.getTransfer(1L, "anotheruser"));
        verifyNoInteractions(transferMapper, cardEncryptionService);
    }
}