package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.fee.ServiceFeeResponseDto;
import com.example.bankcards.model.entity.ServiceFee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServiceFeeMapper {

    @Mapping(source = "account.id", target = "accountId")
    ServiceFeeResponseDto toServiceFeeResponseDto(ServiceFee serviceFee);
}