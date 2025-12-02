package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.notification.NotificationResponseDto;
import com.example.bankcards.model.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponseDto toNotificationResponseDto(Notification notification);
}