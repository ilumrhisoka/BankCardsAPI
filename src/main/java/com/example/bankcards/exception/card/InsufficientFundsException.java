package com.example.bankcards.exception.card;

import com.example.bankcards.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Наследуем от BadRequestException, так как это проблема с запросом/данными
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundsException extends BadRequestException { // Наследуем от BadRequestException
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}