package com.example.bankcards.controller.user;

import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.card.InsufficientFundsException; // Assuming this exception
import com.example.bankcards.exception.transfer.TransferNotFoundException; // Assuming this exception
import com.example.bankcards.model.dto.transfer.TransferRequest;
import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.security.JwtUtil;
import com.example.bankcards.security.SecurityConfig;
import com.example.bankcards.service.transfer.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequest validTransferRequest;
    private TransferResponseDto testTransferResponseDto;
    private final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        validTransferRequest = new TransferRequest();
        validTransferRequest.setFromCardId(1L);
        validTransferRequest.setToCardNumber("1234567890987654");
        validTransferRequest.setAmount(BigDecimal.valueOf(100.00));

        testTransferResponseDto = new TransferResponseDto();
        testTransferResponseDto.setId(1L);
        testTransferResponseDto.setFromCardNumber("123");
        testTransferResponseDto.setToCardNumber("masked-7654");
        testTransferResponseDto.setAmount(BigDecimal.valueOf(100.00));
        testTransferResponseDto.setTransferDate(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void createTransfer_shouldReturnCreated() throws Exception {
        given(transferService.createTransfer(any(TransferRequest.class), eq(TEST_USERNAME)))
                .willReturn(testTransferResponseDto);

        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testTransferResponseDto.getId()))
                .andExpect(jsonPath("$.amount").value(testTransferResponseDto.getAmount()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void createTransfer_withInvalidData_shouldReturnBadRequest() throws Exception {
        TransferRequest invalidRequest = new TransferRequest(); // Missing required fields
        invalidRequest.setFromCardId(1L);
        invalidRequest.setAmount(BigDecimal.valueOf(-10.00)); // Invalid amount

        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void createTransfer_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(transferService.createTransfer(any(TransferRequest.class), eq(TEST_USERNAME)))
                .willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void createTransfer_whenForbidden_shouldReturnForbidden() throws Exception {
        given(transferService.createTransfer(any(TransferRequest.class), eq(TEST_USERNAME)))
                .willThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void createTransfer_whenInsufficientFunds_shouldReturnBadRequest() throws Exception {
        given(transferService.createTransfer(any(TransferRequest.class), eq(TEST_USERNAME)))
                .willThrow(new InsufficientFundsException("Insufficient funds"));

        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTransferRequest)).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getMyTransfers_shouldReturnOkAndListOfTransfers() throws Exception {
        List<TransferResponseDto> transfers = Collections.singletonList(testTransferResponseDto);
        given(transferService.getUserTransfers(eq(TEST_USERNAME))).willReturn(transfers);

        mockMvc.perform(get("/api/user/transfers/my")
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testTransferResponseDto.getId()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getCardTransfers_shouldReturnOkAndListOfTransfers() throws Exception {
        List<TransferResponseDto> transfers = Collections.singletonList(testTransferResponseDto);
        given(transferService.getCardTransfers(anyLong(), eq(TEST_USERNAME))).willReturn(transfers);

        mockMvc.perform(get("/api/user/transfers/card/{cardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testTransferResponseDto.getId()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getCardTransfers_whenCardNotFound_shouldReturnNotFound() throws Exception {
        given(transferService.getCardTransfers(anyLong(), eq(TEST_USERNAME)))
                .willThrow(new CardNotFoundException("Card not found"));

        mockMvc.perform(get("/api/user/transfers/card/{cardId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getCardTransfers_whenForbidden_shouldReturnForbidden() throws Exception {
        given(transferService.getCardTransfers(anyLong(), eq(TEST_USERNAME)))
                .willThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/api/user/transfers/card/{cardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getTransfer_shouldReturnOkAndTransfer() throws Exception {
        given(transferService.getTransfer(anyLong(), eq(TEST_USERNAME))).willReturn(testTransferResponseDto);

        mockMvc.perform(get("/api/user/transfers/{transferId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTransferResponseDto.getId()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getTransfer_whenTransferNotFound_shouldReturnNotFound() throws Exception {
        given(transferService.getTransfer(anyLong(), eq(TEST_USERNAME)))
                .willThrow(new TransferNotFoundException("Transfer not found"));

        mockMvc.perform(get("/api/user/transfers/{transferId}", 99L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME, authorities = "ROLE_USER")
    void getTransfer_whenForbidden_shouldReturnForbidden() throws Exception {
        given(transferService.getTransfer(anyLong(), eq(TEST_USERNAME)))
                .willThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get("/api/user/transfers/{transferId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isForbidden());
    }
}