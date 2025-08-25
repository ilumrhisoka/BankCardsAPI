package com.example.bankcards.controller.user;

import com.example.bankcards.exception.card.CardBlockedException;
import com.example.bankcards.exception.card.CardNotBlockedException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.card.UserCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(UserCardController.class)
class UserCardControllerTest {

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCardService userCardService;

    @Autowired
    private ObjectMapper objectMapper;

    private CardResponseDto testCardResponseDto;
    private final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        testCardResponseDto = new CardResponseDto();
        testCardResponseDto.setId(1L);
        testCardResponseDto.setCardNumber("masked-1234");
        testCardResponseDto.setCardHolder("Test User");
        testCardResponseDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        testCardResponseDto.setBalance(BigDecimal.valueOf(1000.00));
        testCardResponseDto.setCardStatus(CardStatus.ACTIVE);
        testCardResponseDto.setUsername(TEST_USERNAME);
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getUserCards_shouldReturnOkAndPageOfCards() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponseDto> cardPage = new PageImpl<>(Collections.singletonList(testCardResponseDto), pageable, 1);
        given(userCardService.getUserCards(eq(TEST_USERNAME), any(Pageable.class))).willReturn(cardPage);

        mockMvc.perform(get("/api/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testCardResponseDto.getId()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getCardById_shouldReturnOkAndCard() throws Exception {
        given(userCardService.getUserCardById(anyLong(), eq(TEST_USERNAME))).willReturn(testCardResponseDto);

        mockMvc.perform(get("/api/user/cards/{cardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCardResponseDto.getId()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getCardById_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(userCardService.getUserCardById(anyLong(), eq(TEST_USERNAME)))
                .willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/api/user/cards/{cardId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getCardById_whenForbidden_shouldReturnForbidden() throws Exception {
        given(userCardService.getUserCardById(anyLong(), eq(TEST_USERNAME)))
                .willThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/api/user/cards/{cardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestBlockCard_shouldReturnOk() throws Exception {
        doNothing().when(userCardService).requestBlock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/block/{cardId}", 1L).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestBlockCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new CardNotFoundException("Card not found")).when(userCardService).requestBlock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/block/{cardId}", 99L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestBlockCard_whenForbidden_shouldReturnForbidden() throws Exception {
        doThrow(new ForbiddenException("Access denied")).when(userCardService).requestBlock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/block/{cardId}", 1L).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestBlockCard_whenCardAlreadyBlocked_shouldReturnBadRequest() throws Exception {
        doThrow(new CardBlockedException("Card is already blocked")).when(userCardService).requestBlock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/block/{cardId}", 1L).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestUnblockCard_shouldReturnOk() throws Exception {
        doNothing().when(userCardService).requestUnblock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/unblock/{cardId}", 1L).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestUnblockCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new CardNotFoundException("Card not found")).when(userCardService).requestUnblock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/unblock/{cardId}", 99L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestUnblockCard_whenForbidden_shouldReturnForbidden() throws Exception {
        doThrow(new ForbiddenException("Access denied")).when(userCardService).requestUnblock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/unblock/{cardId}", 1L).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void requestUnblockCard_whenCardNotBlocked_shouldReturnBadRequest() throws Exception {
        doThrow(new CardNotBlockedException("Card is not blocked")).when(userCardService).requestUnblock(anyLong(), eq(TEST_USERNAME));

        mockMvc.perform(post("/api/user/cards/unblock/{cardId}", 1L).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getBalance_shouldReturnOkAndBalance() throws Exception {
        BigDecimal totalBalance = BigDecimal.valueOf(2500.00);
        given(userCardService.getTotalBalance(eq(TEST_USERNAME))).willReturn(totalBalance);

        mockMvc.perform(get("/api/user/cards/balance")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(totalBalance));
    }
}