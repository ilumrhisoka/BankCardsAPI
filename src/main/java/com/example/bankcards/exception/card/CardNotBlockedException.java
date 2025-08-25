package com.example.bankcards.exception.card;

import com.example.bankcards.exception.dto.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CardNotBlockedException extends BadRequestException {
    public CardNotBlockedException(String message) {
        super(message);
    }

    public CardNotBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}