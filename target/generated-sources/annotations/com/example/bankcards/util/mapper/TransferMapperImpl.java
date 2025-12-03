package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.transfer.TransferResponseDto;
import com.example.bankcards.model.entity.Transfer;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T19:23:34+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class TransferMapperImpl implements TransferMapper {

    @Override
    public TransferResponseDto toTransferResponseDto(Transfer transfer) {
        if ( transfer == null ) {
            return null;
        }

        TransferResponseDto transferResponseDto = new TransferResponseDto();

        transferResponseDto.setId( transfer.getId() );
        transferResponseDto.setAmount( transfer.getAmount() );
        transferResponseDto.setStatus( transfer.getStatus() );
        transferResponseDto.setTransferDate( transfer.getTransferDate() );
        transferResponseDto.setCreatedAt( transfer.getCreatedAt() );

        return transferResponseDto;
    }
}
