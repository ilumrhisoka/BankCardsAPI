package com.example.bankcards.util.mapper;

import com.example.bankcards.model.dto.notification.NotificationResponseDto;
import com.example.bankcards.model.entity.Notification;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T19:23:34+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.6 (Oracle Corporation)"
)
@Component
public class NotificationMapperImpl implements NotificationMapper {

    @Override
    public NotificationResponseDto toNotificationResponseDto(Notification notification) {
        if ( notification == null ) {
            return null;
        }

        NotificationResponseDto notificationResponseDto = new NotificationResponseDto();

        notificationResponseDto.setId( notification.getId() );
        notificationResponseDto.setMessage( notification.getMessage() );
        notificationResponseDto.setType( notification.getType() );
        notificationResponseDto.setCreatedAt( notification.getCreatedAt() );
        notificationResponseDto.setIsRead( notification.getIsRead() );

        return notificationResponseDto;
    }
}
