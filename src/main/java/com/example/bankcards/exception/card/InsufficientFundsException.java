package com.example.bankcards.exception.card;

import com.example.bankcards.exception.dto.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a transaction or operation fails due to insufficient funds on a card.
 * This exception maps to an HTTP 400 Bad Request status.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundsException extends BadRequestException {

    /**
     * Constructs a new InsufficientFundsException with the specified detail message.
     *
     * @param message the detail message.
     */
    public InsufficientFundsException(String message) {
        super(message);
    }

    /**
     * Constructs a new InsufficientFundsException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}