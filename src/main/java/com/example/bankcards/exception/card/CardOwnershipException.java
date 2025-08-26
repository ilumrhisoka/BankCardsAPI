package com.example.bankcards.exception.card;

import com.example.bankcards.exception.dto.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CardOwnershipException extends ForbiddenException {
    public CardOwnershipException(String message) {
        super(message);
    }

    public CardOwnershipException(String message, Throwable cause) {
        super(message, cause);
    }
}