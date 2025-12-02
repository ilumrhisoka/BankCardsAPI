package com.example.bankcards.model.dto.account;

import com.example.bankcards.model.entity.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for creating a new bank account.
 */
@Data
public class AccountCreateRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    // UserId не требуется, если мы создаем счет для аутентифицированного пользователя,
    // но оставим его для удобства администрирования, если потребуется.
    private Long userId;
}