package com.example.bankcards.service.card;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.card.CardCreateRequest;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.dto.card.CardUpdateRequest;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.CardMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardEncryptionService cardEncryptionService;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CardResponseDto testCardResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", Role.ROLE_USER);
        testUser.setId(1L);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setCardNumber("encrypted-1234");
        testCard.setCardHolder("Test User");
        testCard.setExpiryDate(LocalDate.of(2025, 12, 31));
        testCard.setBalance(BigDecimal.valueOf(1000.00));
        testCard.setCardStatus(CardStatus.ACTIVE);
        testCard.setUser(testUser);

        testCardResponseDto = new CardResponseDto();
        testCardResponseDto.setId(1L);
        testCardResponseDto.setCardNumber("masked-1234");
        testCardResponseDto.setCardHolder("Test User");
        testCardResponseDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        testCardResponseDto.setBalance(BigDecimal.valueOf(1000.00));
        testCardResponseDto.setCardStatus(CardStatus.ACTIVE);
        testCardResponseDto.setUsername(testUser.getUsername());
    }

    @Test
    void createCard_shouldReturnCardResponseDto() {
        CardCreateRequest request = new CardCreateRequest(
                "1234567890123456",
                "Test User",
                LocalDate.of(2025, 12, 31),
                BigDecimal.valueOf(500.00),
                1L
        );

        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(1L);
        mapperReturnDto.setCardHolder("Test User");
        mapperReturnDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        mapperReturnDto.setBalance(BigDecimal.valueOf(1000.00));
        mapperReturnDto.setCardStatus(CardStatus.ACTIVE);
        mapperReturnDto.setUsername(testUser.getUsername());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardEncryptionService.encryptCardNumber(request.getCardNumber())).thenReturn("encrypted-1234");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1234")).thenReturn("masked-1234");


        CardResponseDto result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals(testCardResponseDto.getId(), result.getId());
        assertEquals(testCardResponseDto.getCardHolder(), result.getCardHolder());
        assertEquals(testCardResponseDto.getCardNumber(), result.getCardNumber());
        verify(userRepository).findById(1L);
        verify(cardEncryptionService).encryptCardNumber("1234567890123456");
        verify(cardRepository).save(any(Card.class));
        verify(cardMapper).toCardResponseDto(any(Card.class));
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1234");
    }

    @Test
    void createCard_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        CardCreateRequest request = new CardCreateRequest(
                "1234567890123456",
                "Test User",
                LocalDate.of(2025, 12, 31),
                BigDecimal.valueOf(500.00),
                99L
        );

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.createCard(request));
        verify(userRepository).findById(99L);
        verifyNoInteractions(cardEncryptionService, cardRepository, cardMapper);
    }

    @Test
    void getAllCards_shouldReturnPageOfCardResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(testCard), pageable, 1);

        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(1L);
        mapperReturnDto.setCardHolder("Test User");
        mapperReturnDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        mapperReturnDto.setBalance(BigDecimal.valueOf(1000.00));
        mapperReturnDto.setCardStatus(CardStatus.ACTIVE);
        mapperReturnDto.setUsername(testUser.getUsername());

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponseDto(testCard)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1234")).thenReturn("masked-1234");

        Page<CardResponseDto> result = cardService.getAllCards(pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(testCardResponseDto.getId(), result.getContent().get(0).getId());
        assertEquals(testCardResponseDto.getCardNumber(), result.getContent().get(0).getCardNumber());
        verify(cardRepository).findAll(pageable);
        verify(cardMapper).toCardResponseDto(testCard);
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1234");
    }

    @Test
    void getCardById_whenCardExists_shouldReturnCardResponseDto() {
        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(1L);
        mapperReturnDto.setCardHolder("Test User");
        mapperReturnDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        mapperReturnDto.setBalance(BigDecimal.valueOf(1000.00));
        mapperReturnDto.setCardStatus(CardStatus.ACTIVE);
        mapperReturnDto.setUsername(testUser.getUsername());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardMapper.toCardResponseDto(testCard)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1234")).thenReturn("masked-1234");

        CardResponseDto result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(testCardResponseDto.getId(), result.getId());
        assertEquals(testCardResponseDto.getCardNumber(), result.getCardNumber());
        verify(cardRepository).findById(1L);
        verify(cardMapper).toCardResponseDto(testCard);
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1234");
    }

    @Test
    void getCardById_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(99L));
        verify(cardRepository).findById(99L);
        verifyNoInteractions(cardMapper, cardEncryptionService);
    }

    @Test
    void updateCard_shouldUpdateAndReturnCardResponseDto() {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setCardHolder("Updated Holder");
        request.setCardStatus(CardStatus.BLOCKED);
        request.setBalance(BigDecimal.valueOf(1200.00));

        testCard.setCardHolder(request.getCardHolder());
        testCard.setCardStatus(request.getCardStatus());
        testCard.setBalance(request.getBalance());

        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(testCard.getId());
        mapperReturnDto.setCardHolder(testCard.getCardHolder());
        mapperReturnDto.setCardStatus(testCard.getCardStatus());
        mapperReturnDto.setBalance(testCard.getBalance());
        mapperReturnDto.setExpiryDate(testCard.getExpiryDate());
        mapperReturnDto.setUsername(testCard.getUser().getUsername());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1234")).thenReturn("masked-1234");

        CardResponseDto result = cardService.updateCard(1L, request);

        assertNotNull(result);
        assertEquals("Updated Holder", result.getCardHolder());
        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
        assertEquals(BigDecimal.valueOf(1200.00), result.getBalance());
        assertEquals("masked-1234", result.getCardNumber());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        verify(cardMapper).toCardResponseDto(any(Card.class));
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1234");
    }

    @Test
    void updateCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        CardUpdateRequest request = new CardUpdateRequest();
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard(99L, request));
        verify(cardRepository).findById(99L);
        verifyNoMoreInteractions(cardRepository, cardMapper, cardEncryptionService);
    }

    @Test
    void deleteCard_shouldDeleteCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        doNothing().when(cardRepository).delete(testCard);

        cardService.deleteCard(1L);

        verify(cardRepository).findById(1L);
        verify(cardRepository).delete(testCard);
    }

    @Test
    void deleteCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(99L));
        verify(cardRepository).findById(99L);
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void blockCard_shouldSetStatusToBlockedAndReturnDto() {
        testCard.setCardStatus(CardStatus.BLOCKED);

        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(testCard.getId());
        mapperReturnDto.setCardStatus(testCard.getCardStatus());
        mapperReturnDto.setCardHolder(testCard.getCardHolder());
        mapperReturnDto.setExpiryDate(testCard.getExpiryDate());
        mapperReturnDto.setBalance(testCard.getBalance());
        mapperReturnDto.setUsername(testCard.getUser().getUsername());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1234")).thenReturn("masked-1234");

        CardResponseDto result = cardService.blockCard(1L);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
        assertEquals("masked-1234", result.getCardNumber());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        assertEquals(CardStatus.BLOCKED, testCard.getCardStatus());
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1234");
    }

    @Test
    void blockCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(99L));
        verify(cardRepository).findById(99L);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper, cardEncryptionService);
    }

    @Test
    void activateCard_shouldSetStatusToActiveAndReturnDto() {
        testCard.setCardStatus(CardStatus.BLOCKED);
        testCard.setCardStatus(CardStatus.ACTIVE);

        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(testCard.getId());
        mapperReturnDto.setCardStatus(testCard.getCardStatus());
        mapperReturnDto.setCardHolder(testCard.getCardHolder());
        mapperReturnDto.setExpiryDate(testCard.getExpiryDate());
        mapperReturnDto.setBalance(testCard.getBalance());
        mapperReturnDto.setUsername(testUser.getUsername());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.toCardResponseDto(any(Card.class))).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1234")).thenReturn("masked-1234");

        CardResponseDto result = cardService.activateCard(1L);

        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, result.getCardStatus());
        assertEquals("masked-1234", result.getCardNumber());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        assertEquals(CardStatus.ACTIVE, testCard.getCardStatus());
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1234");
    }

    @Test
    void activateCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.activateCard(99L));
        verify(cardRepository).findById(99L);
        verify(cardRepository, never()).save(any(Card.class));
        verifyNoInteractions(cardMapper, cardEncryptionService);
    }
}