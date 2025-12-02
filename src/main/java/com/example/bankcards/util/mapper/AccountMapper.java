package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.account.AccountResponseDto;
import com.example.bankcards.model.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "user.id", target = "userId")
    AccountResponseDto toAccountResponseDto(Account account);

    // Если потребуется маппинг для создания или обновления, можно добавить сюда.
    // Пока достаточно маппинга для ответа.
}