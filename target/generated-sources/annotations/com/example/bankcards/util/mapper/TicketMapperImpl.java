package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.ticket.TicketResponseDto;
import com.example.bankcards.model.entity.SupportTicket;
import com.example.bankcards.model.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T22:37:49+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class TicketMapperImpl implements TicketMapper {

    @Override
    public TicketResponseDto toTicketResponseDto(SupportTicket ticket) {
        if ( ticket == null ) {
            return null;
        }

        TicketResponseDto ticketResponseDto = new TicketResponseDto();

        ticketResponseDto.setCreatedByUsername( ticketUserUsername( ticket ) );
        ticketResponseDto.setAssignedToAdminUsername( ticketAdminAssignedUsername( ticket ) );
        ticketResponseDto.setId( ticket.getId() );
        ticketResponseDto.setSubject( ticket.getSubject() );
        ticketResponseDto.setDescription( ticket.getDescription() );
        ticketResponseDto.setStatus( ticket.getStatus() );
        ticketResponseDto.setPriority( ticket.getPriority() );
        ticketResponseDto.setCreatedAt( ticket.getCreatedAt() );

        return ticketResponseDto;
    }

    private String ticketUserUsername(SupportTicket supportTicket) {
        if ( supportTicket == null ) {
            return null;
        }
        User user = supportTicket.getUser();
        if ( user == null ) {
            return null;
        }
        String username = user.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }

    private String ticketAdminAssignedUsername(SupportTicket supportTicket) {
        if ( supportTicket == null ) {
            return null;
        }
        User adminAssigned = supportTicket.getAdminAssigned();
        if ( adminAssigned == null ) {
            return null;
        }
        String username = adminAssigned.getUsername();
        if ( username == null ) {
            return null;
        }
        return username;
    }
}
