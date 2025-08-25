package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "fromCardNumber", ignore = true)
    @Mapping(target = "toCardNumber", ignore = true)
    TransferResponseDto toTransferResponseDto(Transfer transfer);
}