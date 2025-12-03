package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.fee.ServiceFeeResponseDto;
import com.example.bankcards.model.entity.Account;
import com.example.bankcards.model.entity.ServiceFee;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T19:23:34+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class ServiceFeeMapperImpl implements ServiceFeeMapper {

    @Override
    public ServiceFeeResponseDto toServiceFeeResponseDto(ServiceFee serviceFee) {
        if ( serviceFee == null ) {
            return null;
        }

        ServiceFeeResponseDto serviceFeeResponseDto = new ServiceFeeResponseDto();

        serviceFeeResponseDto.setAccountId( serviceFeeAccountId( serviceFee ) );
        serviceFeeResponseDto.setId( serviceFee.getId() );
        serviceFeeResponseDto.setFeeType( serviceFee.getFeeType() );
        serviceFeeResponseDto.setAmount( serviceFee.getAmount() );
        serviceFeeResponseDto.setDateCharged( serviceFee.getDateCharged() );
        serviceFeeResponseDto.setIsPaid( serviceFee.getIsPaid() );

        return serviceFeeResponseDto;
    }

    private Long serviceFeeAccountId(ServiceFee serviceFee) {
        if ( serviceFee == null ) {
            return null;
        }
        Account account = serviceFee.getAccount();
        if ( account == null ) {
            return null;
        }
        Long id = account.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
