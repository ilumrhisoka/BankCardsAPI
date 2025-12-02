package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.ticket.TicketResponseDto;
import com.example.bankcards.model.entity.SupportTicket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "user.username", target = "createdByUsername")
    @Mapping(source = "adminAssigned.username", target = "assignedToAdminUsername")
    TicketResponseDto toTicketResponseDto(SupportTicket ticket);
}