package com.example.bankcards.exception.card;

import com.example.bankcards.exception.dto.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an operation cannot be performed due to the card's current status.
 * For example, attempting to use a blocked or expired card.
 * This exception maps to an HTTP 400 Bad Request status.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CardStatusException extends BadRequestException {

    /**
     * Constructs a new CardStatusException with the specified detail message.
     *
     * @param message the detail message.
     */
    public CardStatusException(String message) {
        super(message);
    }

    /**
     * Constructs a new CardStatusException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public CardStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}