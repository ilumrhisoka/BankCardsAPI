package com.example.bankcards.service.account;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.account.AccountCreateRequest;
import com.example.bankcards.model.dto.account.AccountResponseDto;
import com.example.bankcards.model.entity.Account;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.model.entity.enums.AccountType;
import com.example.bankcards.repository.AccountRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing bank accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    /**
     * Creates a new bank account for a specified user.
     *
     * @param request The request containing account details.
     * @param username The username of the authenticated user creating the account.
     * @return DTO of the newly created account.
     */
    @Transactional
    public AccountResponseDto createAccount(AccountCreateRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        Account account = new Account();
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setAccountNumber(generateUniqueAccountNumber()); // Генерация уникального номера счета

        Account savedAccount = accountRepository.save(account);
        log.info("Created new account {} for user {}", savedAccount.getAccountNumber(), username);

        return accountMapper.toAccountResponseDto(savedAccount);
    }

    /**
     * Retrieves all accounts belonging to the authenticated user.
     *
     * @param username The username of the owner.
     * @return List of account DTOs.
     */
    public List<AccountResponseDto> getMyAccounts(String username) {
        List<Account> accounts = accountRepository.findByUserUsername(username);
        return accounts.stream()
                .map(accountMapper::toAccountResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the total balance across all accounts for a user.
     *
     * @param username The username of the owner.
     * @return Total balance.
     */
    public BigDecimal getTotalBalanceAcrossAccounts(String username) {
        return accountRepository.findByUserUsername(username).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Finds an existing CHECKING account for the user or creates a new one if none exists.
     * Used typically when creating a new card that needs to be linked to an account.
     *
     * @param user The user entity.
     * @return An existing or newly created default CHECKING account.
     */
    @Transactional
    public Account findOrCreateDefaultAccount(User user) {
        // Try to find an existing CHECKING account
        // Используем список accounts, который загружается лениво, но MapStruct его не использует,
        // поэтому для этой операции нам нужно убедиться, что список доступен или использовать репозиторий.
        // Поскольку User уже загружен, используем его список:

        return user.getAccounts().stream()
                .filter(a -> a.getAccountType() == AccountType.CHECKING)
                .findFirst()
                .orElseGet(() -> {
                    // If no CHECKING account exists, create one
                    Account account = new Account();
                    account.setAccountType(AccountType.CHECKING);
                    account.setBalance(BigDecimal.ZERO);
                    account.setUser(user);
                    account.setAccountNumber(generateUniqueAccountNumber());
                    log.info("Creating default CHECKING account {} for user {}", account.getAccountNumber(), user.getUsername());
                    return accountRepository.save(account);
                });
    }

    /**
     * Helper method to generate a unique account number (e.g., 16 digits).
     * NOTE: In a production environment, this requires robust, thread-safe, and collision-resistant logic.
     */
    private String generateUniqueAccountNumber() {
        // Простой пример генерации, в реальности нужна проверка на уникальность в БД
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
    }
}