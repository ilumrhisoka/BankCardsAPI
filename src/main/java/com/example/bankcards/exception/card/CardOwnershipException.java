package com.example.bankcards.exception.card;

import com.example.bankcards.exception.dto.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts an operation on a card they do not own,
 * or for which they do not have the necessary permissions.
 * This exception maps to an HTTP 403 Forbidden status.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class CardOwnershipException extends ForbiddenException {

    /**
     * Constructs a new CardOwnershipException with the specified detail message.
     *
     * @param message the detail message.
     */
    public CardOwnershipException(String message) {
        super(message);
    }

    /**
     * Constructs a new CardOwnershipException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public CardOwnershipException(String message, Throwable cause) {
        super(message, cause);
    }
}