package com.example.bankcards.service.transfer;

import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.dto.BadRequestException;
import com.example.bankcards.exception.dto.ForbiddenException;
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
import static org.mockito.ArgumentMatchers.anyString;
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
    private TransferResponseDto expectedTransferResponseDto;

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

        lenient().when(cardEncryptionService.matchesCardNumber(anyString(), anyString())).thenReturn(false);
        lenient().when(cardEncryptionService.matchesCardNumber("plain_1234", "encrypted_1234")).thenReturn(true);
        lenient().when(cardEncryptionService.matchesCardNumber("plain_5678", "encrypted_5678")).thenReturn(true);

        expectedTransferResponseDto = new TransferResponseDto();
        expectedTransferResponseDto.setId(1L);
        expectedTransferResponseDto.setFromCardNumber("masked_1234");
        expectedTransferResponseDto.setToCardNumber("masked_5678");
        expectedTransferResponseDto.setAmount(new BigDecimal("100.00"));
        expectedTransferResponseDto.setStatus(TransferStatus.SUCCESS);
        expectedTransferResponseDto.setTransferDate(LocalDateTime.now());
        expectedTransferResponseDto.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createTransfer_whenValid_shouldSucceed() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        TransferResponseDto mapperReturnDto = new TransferResponseDto();
        mapperReturnDto.setId(1L);
        mapperReturnDto.setAmount(transferRequest.getAmount());
        mapperReturnDto.setStatus(TransferStatus.SUCCESS);
        mapperReturnDto.setTransferDate(LocalDateTime.now());
        mapperReturnDto.setCreatedAt(LocalDateTime.now());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCard));
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
        verify(transferMapper).toTransferResponseDto(any(Transfer.class));
        verify(cardEncryptionService, times(4)).getMaskedCardNumber(anyString());
    }

    @Test
    void createTransfer_whenInsufficientFunds_shouldThrowInsufficientFundsException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        fromCard.setBalance(new BigDecimal("50.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCard));

        assertThrows(InsufficientFundsException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verify(cardRepository, never()).save(any());
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenFromCardBlocked_shouldThrowCardBlockedException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        fromCard.setCardStatus(CardStatus.BLOCKED); // Карта заблокирована
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        assertThrows(CardBlockedException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_toSameCard_shouldThrowBadRequestException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_1234");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard));

        assertThrows(BadRequestException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verifyNoInteractions(transferMapper);
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
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenToCardNotFound_shouldThrowCardNotFoundException() {
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("nonexistent_card_number");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(Collections.singletonList(fromCard));

        assertThrows(CardNotFoundException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenToCardDoesNotBelongToUser_shouldThrowForbiddenException() {
        Card toCardAnotherUser = new Card();
        toCardAnotherUser.setId(3L);
        toCardAnotherUser.setCardNumber("encrypted_9999");
        toCardAnotherUser.setBalance(new BigDecimal("100.00"));
        toCardAnotherUser.setCardStatus(CardStatus.ACTIVE);
        toCardAnotherUser.setUser(anotherUser);

        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_9999");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCardAnotherUser));
        lenient().when(cardEncryptionService.matchesCardNumber("plain_9999", "encrypted_9999")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verifyNoInteractions(transferMapper);
    }

    @Test
    void createTransfer_whenToCardBlocked_shouldThrowCardBlockedException() {
        toCard.setCardStatus(CardStatus.BLOCKED);

        transferRequest.setFromCardId(1L);
        transferRequest.setToCardNumber("plain_5678");
        transferRequest.setAmount(new BigDecimal("100.00"));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findAll()).thenReturn(List.of(fromCard, toCard));

        assertThrows(CardBlockedException.class, () -> {
            transferService.createTransfer(transferRequest, "testuser");
        });
        verifyNoInteractions(transferMapper);
    }

    @Test
    void getUserTransfers_shouldReturnListOfTransferResponseDto() {
        Transfer testTransfer = new Transfer();
        testTransfer.setId(1L);
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);
        testTransfer.setAmount(new BigDecimal("50.00"));
        testTransfer.setStatus(TransferStatus.SUCCESS);
        testTransfer.setTransferDate(LocalDateTime.now());
        testTransfer.setCreatedAt(LocalDateTime.now());

        TransferResponseDto mapperReturnDto = new TransferResponseDto();
        mapperReturnDto.setId(testTransfer.getId());
        mapperReturnDto.setAmount(testTransfer.getAmount());
        mapperReturnDto.setStatus(testTransfer.getStatus());
        mapperReturnDto.setTransferDate(testTransfer.getTransferDate());
        mapperReturnDto.setCreatedAt(testTransfer.getCreatedAt());

        when(transferRepository.findByUserUsername("testuser")).thenReturn(Collections.singletonList(testTransfer));
        when(transferMapper.toTransferResponseDto(testTransfer)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted_1234")).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber("encrypted_5678")).thenReturn("masked_5678");

        List<TransferResponseDto> result = transferService.getUserTransfers("testuser");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("masked_1234", result.get(0).getFromCardNumber());
        assertEquals("masked_5678", result.get(0).getToCardNumber());
        verify(transferRepository).findByUserUsername("testuser");
        verify(transferMapper).toTransferResponseDto(testTransfer);
        verify(cardEncryptionService, times(2)).getMaskedCardNumber(anyString());
    }

    @Test
    void getCardTransfers_shouldReturnListOfTransferResponseDto() {
        Transfer testTransfer = new Transfer();
        testTransfer.setId(1L);
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);
        testTransfer.setAmount(new BigDecimal("50.00"));
        testTransfer.setStatus(TransferStatus.SUCCESS);
        testTransfer.setTransferDate(LocalDateTime.now());
        testTransfer.setCreatedAt(LocalDateTime.now());

        TransferResponseDto mapperReturnDto = new TransferResponseDto();
        mapperReturnDto.setId(testTransfer.getId());
        mapperReturnDto.setAmount(testTransfer.getAmount());
        mapperReturnDto.setStatus(testTransfer.getStatus());
        mapperReturnDto.setTransferDate(testTransfer.getTransferDate());
        mapperReturnDto.setCreatedAt(testTransfer.getCreatedAt());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(transferRepository.findByCardId(1L)).thenReturn(Collections.singletonList(testTransfer));
        when(transferMapper.toTransferResponseDto(testTransfer)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted_1234")).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber("encrypted_5678")).thenReturn("masked_5678");

        List<TransferResponseDto> result = transferService.getCardTransfers(1L, "testuser");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("masked_1234", result.get(0).getFromCardNumber());
        assertEquals("masked_5678", result.get(0).getToCardNumber());
        verify(cardRepository).findById(1L);
        verify(transferRepository).findByCardId(1L);
        verify(transferMapper).toTransferResponseDto(testTransfer);
        verify(cardEncryptionService, times(2)).getMaskedCardNumber(anyString());
    }

    @Test
    void getCardTransfers_whenCardNotFound_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> transferService.getCardTransfers(99L, "testuser"));
        verify(cardRepository).findById(99L);
        verifyNoInteractions(transferRepository, transferMapper, cardEncryptionService);
    }

    @Test
    void getCardTransfers_whenCardDoesNotBelongToUser_shouldThrowForbiddenException() {
        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(3L);
        cardOfAnotherUser.setUser(anotherUser);

        when(cardRepository.findById(3L)).thenReturn(Optional.of(cardOfAnotherUser));

        assertThrows(ForbiddenException.class, () -> transferService.getCardTransfers(3L, "testuser"));
        verify(cardRepository).findById(3L);
        verifyNoInteractions(transferRepository, transferMapper, cardEncryptionService);
    }

    @Test
    void getTransfer_shouldReturnTransferResponseDto() {
        Transfer testTransfer = new Transfer();
        testTransfer.setId(1L);
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);
        testTransfer.setAmount(new BigDecimal("50.00"));
        testTransfer.setStatus(TransferStatus.SUCCESS);
        testTransfer.setTransferDate(LocalDateTime.now());
        testTransfer.setCreatedAt(LocalDateTime.now());

        TransferResponseDto mapperReturnDto = new TransferResponseDto();
        mapperReturnDto.setId(testTransfer.getId());
        mapperReturnDto.setAmount(testTransfer.getAmount());
        mapperReturnDto.setStatus(testTransfer.getStatus());
        mapperReturnDto.setTransferDate(testTransfer.getTransferDate());
        mapperReturnDto.setCreatedAt(testTransfer.getCreatedAt());

        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));
        when(transferMapper.toTransferResponseDto(testTransfer)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted_1234")).thenReturn("masked_1234");
        when(cardEncryptionService.getMaskedCardNumber("encrypted_5678")).thenReturn("masked_5678");

        TransferResponseDto result = transferService.getTransfer(1L, "testuser");

        assertNotNull(result);
        assertEquals("masked_1234", result.getFromCardNumber());
        assertEquals("masked_5678", result.getToCardNumber());
        verify(transferRepository).findById(1L);
        verify(transferMapper).toTransferResponseDto(testTransfer);
        verify(cardEncryptionService, times(2)).getMaskedCardNumber(anyString());
    }

    @Test
    void getTransfer_whenUserNotParticipant_shouldThrowForbiddenException() {
        User thirdUser = new User("thirduser", "third@example.com", "pass", Role.ROLE_USER);
        thirdUser.setId(3L);

        Card thirdUserCard = new Card();
        thirdUserCard.setId(3L);
        thirdUserCard.setCardNumber("encrypted_9999");
        thirdUserCard.setUser(thirdUser);

        Transfer testTransfer = new Transfer();
        testTransfer.setId(1L);
        testTransfer.setFromCard(fromCard);
        testTransfer.setToCard(toCard);

        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));

        assertThrows(ForbiddenException.class, () -> transferService.getTransfer(1L, "thirduser"));
        verify(transferRepository).findById(1L);
        verifyNoInteractions(transferMapper, cardEncryptionService);
    }
}