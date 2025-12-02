package com.example.bankcards.controller.user;

import com.example.bankcards.model.dto.account.AccountCreateRequest;
import com.example.bankcards.model.dto.account.AccountResponseDto;
import com.example.bankcards.model.dto.fee.ServiceFeeResponseDto;
import com.example.bankcards.service.account.AccountService;
import com.example.bankcards.service.fee.ServiceFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for managing user's bank accounts.
 * All operations require the authenticated user.
 */
@RestController
@RequestMapping("/api/user/accounts")
@Tag(name = "User Account Management", description = "Operations related to managing user's bank accounts and associated fees")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final ServiceFeeService serviceFeeService; // NEW: Интеграция сервиса комиссий

    /**
     * Creates a new bank account for the authenticated user.
     *
     * @param request The request containing account type.
     * @param authentication The authentication object.
     * @return The created account details.
     */
    @Operation(summary = "Create a new bank account")
    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(
            @Valid @RequestBody AccountCreateRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        AccountResponseDto newAccount = accountService.createAccount(request, username);
        return new ResponseEntity<>(newAccount, HttpStatus.CREATED);
    }

    /**
     * Retrieves a list of all bank accounts owned by the authenticated user.
     *
     * @param authentication The authentication object.
     * @return List of account details.
     */
    @Operation(summary = "Get all accounts for the current user")
    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getMyAccounts(Authentication authentication) {
        String username = authentication.getName();
        List<AccountResponseDto> accounts = accountService.getMyAccounts(username);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Retrieves the total balance across all accounts owned by the authenticated user.
     *
     * @param authentication The authentication object.
     * @return Total balance.
     */
    @Operation(summary = "Get total balance across all user accounts")
    @GetMapping("/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance(Authentication authentication) {
        String username = authentication.getName();
        BigDecimal totalBalance = accountService.getTotalBalanceAcrossAccounts(username);
        return ResponseEntity.ok(totalBalance);
    }

    /**
     * Retrieves all fees associated with the authenticated user's accounts.
     */
    @Operation(summary = "Get all service fees charged to user's accounts")
    @GetMapping("/fees")
    public ResponseEntity<List<ServiceFeeResponseDto>> getMyFees(Authentication authentication) {
        String username = authentication.getName();
        List<ServiceFeeResponseDto> fees = serviceFeeService.getMyFees(username);
        return ResponseEntity.ok(fees);
    }

    /**
     * Retrieves all unpaid fees for a specific account.
     */
    @Operation(summary = "Get unpaid fees for a specific account")
    @GetMapping("/{accountId}/unpaid-fees")
    public ResponseEntity<List<ServiceFeeResponseDto>> getUnpaidFees(
            @PathVariable Long accountId,
            Authentication authentication) {
        String username = authentication.getName();
        List<ServiceFeeResponseDto> fees = serviceFeeService.getUnpaidFeesForAccount(accountId, username);
        return ResponseEntity.ok(fees);
    }
}