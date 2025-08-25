package com.example.bankcards.service.card;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.Card;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.model.entity.enums.Role;
import com.example.bankcards.repository.CardRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardEncryptionService cardEncryptionService;

    @InjectMocks
    private UserCardService userCardService;

    private User testUser;
    private User anotherUser;
    private Card activeCard;
    private Card blockedCard;
    private CardResponseDto activeCardResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password", Role.ROLE_USER);
        testUser.setId(1L);

        anotherUser = new User("anotheruser", "another@example.com", "password", Role.ROLE_USER);
        anotherUser.setId(2L);

        activeCard = new Card();
        activeCard.setId(1L);
        activeCard.setCardNumber("encrypted-1111");
        activeCard.setCardHolder("Test User");
        activeCard.setExpiryDate(LocalDate.of(2025, 12, 31));
        activeCard.setBalance(BigDecimal.valueOf(1000.00));
        activeCard.setCardStatus(CardStatus.ACTIVE);
        activeCard.setUser(testUser);

        blockedCard = new Card();
        blockedCard.setId(2L);
        blockedCard.setCardNumber("encrypted-2222");
        blockedCard.setCardHolder("Test User");
        blockedCard.setExpiryDate(LocalDate.of(2026, 1, 1));
        blockedCard.setBalance(BigDecimal.valueOf(500.00));
        blockedCard.setCardStatus(CardStatus.BLOCKED);
        blockedCard.setUser(testUser);

        activeCardResponseDto = new CardResponseDto();
        activeCardResponseDto.setId(1L);
        activeCardResponseDto.setCardNumber("masked-1111");
        activeCardResponseDto.setCardHolder("Test User");
        activeCardResponseDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        activeCardResponseDto.setBalance(BigDecimal.valueOf(1000.00));
        activeCardResponseDto.setCardStatus(CardStatus.ACTIVE);
        activeCardResponseDto.setUsername(testUser.getUsername());
    }

    @Test
    void getTotalBalance_whenNoActiveCards_shouldReturnZero() {
        when(cardRepository.findByUserUsername("testuser")).thenReturn(Collections.singletonList(blockedCard));

        BigDecimal totalBalance = userCardService.getTotalBalance("testuser");

        assertNotNull(totalBalance);
        assertEquals(BigDecimal.ZERO, totalBalance);
        verify(cardRepository).findByUserUsername("testuser");
    }

    @Test
    void getTotalBalance_whenNoCardsForUser_shouldReturnZero() {
        when(cardRepository.findByUserUsername("nonexistentuser")).thenReturn(Collections.emptyList());

        BigDecimal totalBalance = userCardService.getTotalBalance("nonexistentuser");

        assertNotNull(totalBalance);
        assertEquals(BigDecimal.ZERO, totalBalance);
        verify(cardRepository).findByUserUsername("nonexistentuser");
    }

    @Test
    void requestUnblock_whenCardNotFound_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> userCardService.requestUnblock(99L, "testuser"));
        verify(cardRepository).findById(99L);
        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void requestUnblock_whenCardDoesNotBelongToUser_shouldThrowForbiddenException() {
        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(3L);
        cardOfAnotherUser.setUser(anotherUser);
        cardOfAnotherUser.setCardNumber("encrypted-3333");
        cardOfAnotherUser.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(3L)).thenReturn(Optional.of(cardOfAnotherUser));

        assertThrows(ForbiddenException.class, () -> userCardService.requestUnblock(3L, "testuser"));
        verify(cardRepository).findById(3L);
        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void requestUnblock_whenCardIsNotBlocked_shouldThrowCardNotBlockedException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));

        assertThrows(CardStatusException.class, () -> userCardService.requestUnblock(1L, "testuser"));
        verify(cardRepository).findById(1L);
        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void requestUnblock_shouldLogUnblockRequest() {
        when(cardRepository.findById(2L)).thenReturn(Optional.of(blockedCard));
        when(cardEncryptionService.getMaskedCardNumber("encrypted-2222")).thenReturn("masked-2222");

        assertDoesNotThrow(() -> userCardService.requestUnblock(2L, "testuser"));
        verify(cardRepository).findById(2L);
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-2222");
    }

    @Test
    void requestBlock_shouldLogBlockRequest() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1111")).thenReturn("masked-1111");

        assertDoesNotThrow(() -> userCardService.requestBlock(1L, "testuser"));
        verify(cardRepository).findById(1L);
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1111");
    }

    @Test
    void requestBlock_whenCardNotFound_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> userCardService.requestBlock(99L, "testuser"));
        verify(cardRepository).findById(99L);
        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void requestBlock_whenCardDoesNotBelongToUser_shouldThrowForbiddenException() {
        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(3L);
        cardOfAnotherUser.setUser(anotherUser);
        cardOfAnotherUser.setCardNumber("encrypted-3333");
        cardOfAnotherUser.setCardStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(3L)).thenReturn(Optional.of(cardOfAnotherUser));

        assertThrows(ForbiddenException.class, () -> userCardService.requestBlock(3L, "testuser"));
        verify(cardRepository).findById(3L);
        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void requestBlock_whenCardIsAlreadyBlocked_shouldThrowCardBlockedException() {
        when(cardRepository.findById(2L)).thenReturn(Optional.of(blockedCard));

        assertThrows(CardStatusException.class, () -> userCardService.requestBlock(2L, "testuser"));
        verify(cardRepository).findById(2L);
        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void getUserCards_shouldReturnPageOfCardResponseDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Collections.singletonList(activeCard), pageable, 1);

        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(1L);
        mapperReturnDto.setCardHolder("Test User");
        mapperReturnDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        mapperReturnDto.setBalance(BigDecimal.valueOf(1000.00));
        mapperReturnDto.setCardStatus(CardStatus.ACTIVE);
        mapperReturnDto.setUsername(testUser.getUsername());

        when(cardRepository.findByUserUsernamePageable("testuser", pageable)).thenReturn(cardPage);
        when(cardMapper.toCardResponseDto(activeCard)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1111")).thenReturn("masked-1111");

        Page<CardResponseDto> result = userCardService.getUserCards("testuser", pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(activeCardResponseDto.getId(), result.getContent().get(0).getId());
        assertEquals(activeCardResponseDto.getCardNumber(), result.getContent().get(0).getCardNumber());
        verify(cardRepository).findByUserUsernamePageable("testuser", pageable);
        verify(cardMapper).toCardResponseDto(activeCard);
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1111");
    }

    @Test
    void getUserCardById_whenCardExistsAndBelongsToUser_shouldReturnCardResponseDto() {
        CardResponseDto mapperReturnDto = new CardResponseDto();
        mapperReturnDto.setId(1L);
        mapperReturnDto.setCardHolder("Test User");
        mapperReturnDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        mapperReturnDto.setBalance(BigDecimal.valueOf(1000.00));
        mapperReturnDto.setCardStatus(CardStatus.ACTIVE);
        mapperReturnDto.setUsername(testUser.getUsername());

        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(cardMapper.toCardResponseDto(activeCard)).thenReturn(mapperReturnDto);
        when(cardEncryptionService.getMaskedCardNumber("encrypted-1111")).thenReturn("masked-1111");

        CardResponseDto result = userCardService.getUserCardById(1L, "testuser");

        assertNotNull(result);
        assertEquals(activeCardResponseDto.getId(), result.getId());
        assertEquals(activeCardResponseDto.getCardNumber(), result.getCardNumber());
        verify(cardRepository).findById(1L);
        verify(cardMapper).toCardResponseDto(activeCard);
        verify(cardEncryptionService).getMaskedCardNumber("encrypted-1111");
    }

    @Test
    void getUserCardById_whenCardNotFound_shouldThrowCardNotFoundException() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> userCardService.getUserCardById(99L, "testuser"));
        verify(cardRepository).findById(99L);
        verifyNoInteractions(cardMapper, cardEncryptionService);
    }

    @Test
    void getUserCardById_whenCardDoesNotBelongToUser_shouldThrowForbiddenException() {
        Card cardOfAnotherUser = new Card();
        cardOfAnotherUser.setId(3L);
        cardOfAnotherUser.setUser(anotherUser);
        cardOfAnotherUser.setCardNumber("encrypted-3333");

        when(cardRepository.findById(3L)).thenReturn(Optional.of(cardOfAnotherUser));

        assertThrows(ForbiddenException.class, () -> userCardService.getUserCardById(3L, "testuser"));
        verify(cardRepository).findById(3L);
        verifyNoInteractions(cardMapper, cardEncryptionService);
    }
}