package com.example.bankcards.service.card;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.model.dto.card.CardCreateRequest;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.dto.card.CardUpdateRequest;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.CardDtoMapper;
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
    private CardDtoMapper cardDtoMapper;

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

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardEncryptionService.encryptCardNumber(request.getCardNumber())).thenReturn("encrypted-1234");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardDtoMapper.toCardResponseDto(any(Card.class))).thenReturn(testCardResponseDto);

        CardResponseDto result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals(testCardResponseDto.getId(), result.getId());
        assertEquals(testCardResponseDto.getCardHolder(), result.getCardHolder());
        verify(userRepository).findById(1L);
        verify(cardEncryptionService).encryptCardNumber("1234567890123456");
        verify(cardRepository).save(any(Card.class));
        verify(cardDtoMapper).toCardResponseDto(any(Card.class));
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

        assertThrows(UsernameNotFoundException.class, () -> cardService.createCard(request));
        verify(userRepository).findById(99L);
        verifyNoInteractions(cardEncryptionService, cardRepository, cardDtoMapper);
    }

    @Test
    void getAllCards_shouldReturnPageOfCardResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(testCard), pageable, 1);
        Page<CardResponseDto> expectedDtoPage = new PageImpl<>(Collections.singletonList(testCardResponseDto), pageable, 1);

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardDtoMapper.toCardResponseDto(testCard)).thenReturn(testCardResponseDto);

        Page<CardResponseDto> result = cardService.getAllCards(pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(testCardResponseDto.getId(), result.getContent().get(0).getId());
        verify(cardRepository).findAll(pageable);
        verify(cardDtoMapper).toCardResponseDto(testCard);
    }

    @Test
    void getCardById_whenCardExists_shouldReturnCardResponseDto() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardDtoMapper.toCardResponseDto(testCard)).thenReturn(testCardResponseDto);

        CardResponseDto result = cardService.getCardById(1L);

        assertNotNull(result);
        assertEquals(testCardResponseDto.getId(), result.getId());
        verify(cardRepository).findById(1L);
        verify(cardDtoMapper).toCardResponseDto(testCard);
    }

    @Test
    void getCardById_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(99L));
        verify(cardRepository).findById(99L);
        verifyNoInteractions(cardDtoMapper);
    }

    @Test
    void updateCard_shouldUpdateAndReturnCardResponseDto() {
        CardUpdateRequest request = new CardUpdateRequest();
        request.setCardHolder("Updated Holder");
        request.setCardStatus(CardStatus.BLOCKED);
        request.setBalance(BigDecimal.valueOf(1200.00));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved card
        when(cardDtoMapper.toCardResponseDto(any(Card.class))).thenAnswer(invocation -> {
            Card updatedCard = invocation.getArgument(0);
            CardResponseDto dto = new CardResponseDto();
            dto.setId(updatedCard.getId());
            dto.setCardHolder(updatedCard.getCardHolder());
            dto.setCardStatus(updatedCard.getCardStatus());
            dto.setBalance(updatedCard.getBalance());
            // Copy other relevant fields for DTO
            return dto;
        });

        CardResponseDto result = cardService.updateCard(1L, request);

        assertNotNull(result);
        assertEquals("Updated Holder", result.getCardHolder());
        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
        assertEquals(BigDecimal.valueOf(1200.00), result.getBalance());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard); // Verify save was called with the modified testCard
        verify(cardDtoMapper).toCardResponseDto(any(Card.class));
    }

    @Test
    void updateCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        CardUpdateRequest request = new CardUpdateRequest();
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard(99L, request));
        verify(cardRepository).findById(99L);
        verifyNoMoreInteractions(cardRepository); // Ensure save is not called
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
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardDtoMapper.toCardResponseDto(any(Card.class))).thenAnswer(invocation -> {
            Card blockedCard = invocation.getArgument(0);
            CardResponseDto dto = new CardResponseDto();
            dto.setId(blockedCard.getId());
            dto.setCardStatus(blockedCard.getCardStatus());
            return dto;
        });

        CardResponseDto result = cardService.blockCard(1L);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, result.getCardStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        assertEquals(CardStatus.BLOCKED, testCard.getCardStatus()); // Verify entity state changed
    }

    @Test
    void blockCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(99L));
        verify(cardRepository).findById(99L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCard_shouldSetStatusToActiveAndReturnDto() {
        testCard.setCardStatus(CardStatus.BLOCKED); // Set initial status to blocked for activation test
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardDtoMapper.toCardResponseDto(any(Card.class))).thenAnswer(invocation -> {
            Card activatedCard = invocation.getArgument(0);
            CardResponseDto dto = new CardResponseDto();
            dto.setId(activatedCard.getId());
            dto.setCardStatus(activatedCard.getCardStatus());
            return dto;
        });

        CardResponseDto result = cardService.activateCard(1L);

        assertNotNull(result);
        assertEquals(CardStatus.ACTIVE, result.getCardStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
        assertEquals(CardStatus.ACTIVE, testCard.getCardStatus()); // Verify entity state changed
    }

    @Test
    void activateCard_whenCardDoesNotExist_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.activateCard(99L));
        verify(cardRepository).findById(99L);
        verify(cardRepository, never()).save(any(Card.class));
    }
}