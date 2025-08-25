package com.example.bankcards.controller.admin;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.card.CardCreateRequest;
import com.example.bankcards.model.dto.card.CardResponseDto;
import com.example.bankcards.model.dto.card.CardUpdateRequest;
import com.example.bankcards.model.entity.enums.CardStatus;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.card.CardService;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(AdminCardController.class)
class AdminCardControllerTest {

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private CardResponseDto testCardResponseDto;
    private CardCreateRequest validCardCreateRequest;
    private CardUpdateRequest validCardUpdateRequest;

    @BeforeEach
    void setUp() {
        testCardResponseDto = new CardResponseDto();
        testCardResponseDto.setId(1L);
        testCardResponseDto.setCardNumber("masked-1234");
        testCardResponseDto.setCardHolder("Test User");
        testCardResponseDto.setExpiryDate(LocalDate.of(2025, 12, 31));
        testCardResponseDto.setBalance(BigDecimal.valueOf(1000.00));
        testCardResponseDto.setCardStatus(CardStatus.ACTIVE);
        testCardResponseDto.setUsername("testUsername");

        validCardCreateRequest = new CardCreateRequest();
        validCardCreateRequest.setUserId(1L);
        validCardCreateRequest.setCardNumber("1234567890123456");
        validCardCreateRequest.setCardHolder("New Card Holder");
        validCardCreateRequest.setExpiryDate(LocalDate.of(2026, 1, 1));
        validCardCreateRequest.setBalance(BigDecimal.valueOf(500.00));

        validCardUpdateRequest = new CardUpdateRequest();
        validCardUpdateRequest.setCardHolder("Updated Holder");
        validCardUpdateRequest.setCardStatus(CardStatus.BLOCKED);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createCard_shouldReturnCreated() throws Exception {
        given(cardService.createCard(any(CardCreateRequest.class))).willReturn(testCardResponseDto);

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardCreateRequest)).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testCardResponseDto.getId()))
                .andExpect(jsonPath("$.cardNumber").value(testCardResponseDto.getCardNumber()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createCard_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Missing card number
        CardCreateRequest invalidRequest = new CardCreateRequest();
        invalidRequest.setUserId(1L);
        invalidRequest.setCardHolder("Invalid User");
        invalidRequest.setExpiryDate(LocalDate.of(2026, 1, 1));
        invalidRequest.setBalance(BigDecimal.valueOf(100.00));

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createCard_whenUserNotFound_shouldReturnNotFound() throws Exception {
        given(cardService.createCard(any(CardCreateRequest.class)))
                .willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardCreateRequest)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getAllCards_shouldReturnOkAndPageOfCards() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CardResponseDto> cardPage = new PageImpl<>(Collections.singletonList(testCardResponseDto), pageable, 1);
        given(cardService.getAllCards(any(Pageable.class))).willReturn(cardPage);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(testCardResponseDto.getId()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getCardById_shouldReturnOkAndCard() throws Exception {
        given(cardService.getCardById(anyLong())).willReturn(testCardResponseDto);

        mockMvc.perform(get("/api/admin/cards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCardResponseDto.getId()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getCardById_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(cardService.getCardById(anyLong())).willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/api/admin/cards/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateCard_shouldReturnOkAndUpdatedCard() throws Exception {
        CardResponseDto updatedDto = new CardResponseDto();
        updatedDto.setId(1L);
        updatedDto.setCardHolder(validCardUpdateRequest.getCardHolder());
        updatedDto.setCardStatus(validCardUpdateRequest.getCardStatus());
        given(cardService.updateCard(anyLong(), any(CardUpdateRequest.class))).willReturn(updatedDto);

        mockMvc.perform(put("/api/admin/cards/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardUpdateRequest)).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardHolder").value(updatedDto.getCardHolder()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(cardService.updateCard(anyLong(), any(CardUpdateRequest.class)))
                .willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(put("/api/admin/cards/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardUpdateRequest)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteCard_shouldReturnNoContent() throws Exception {
        doNothing().when(cardService).deleteCard(anyLong());

        mockMvc.perform(delete("/api/admin/cards/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        doThrow(new CardNotFoundException("Card not found")).when(cardService).deleteCard(anyLong());

        mockMvc.perform(delete("/api/admin/cards/{id}", 99L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void blockCard_shouldReturnOkAndBlockedCard() throws Exception {
        CardResponseDto blockedDto = new CardResponseDto();
        blockedDto.setId(1L);
        blockedDto.setCardStatus(CardStatus.BLOCKED);
        given(cardService.blockCard(anyLong())).willReturn(blockedDto);

        mockMvc.perform(post("/api/admin/cards/{id}/block", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardStatus").value(CardStatus.BLOCKED.name()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void blockCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(cardService.blockCard(anyLong())).willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/api/admin/cards/{id}/block", 99L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void activateCard_shouldReturnOkAndActivatedCard() throws Exception {
        CardResponseDto activatedDto = new CardResponseDto();
        activatedDto.setId(1L);
        activatedDto.setCardStatus(CardStatus.ACTIVE);
        given(cardService.activateCard(anyLong())).willReturn(activatedDto);

        mockMvc.perform(post("/api/admin/cards/{id}/activate", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardStatus").value(CardStatus.ACTIVE.name()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void activateCard_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(cardService.activateCard(anyLong())).willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/api/admin/cards/{id}/activate", 99L).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void createCard_asUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCardCreateRequest)).with(csrf()))
                .andExpect(status().isForbidden());
    }
}