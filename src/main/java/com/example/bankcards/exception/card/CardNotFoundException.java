package com.example.bankcards.exception.card;

import com.example.bankcards.exception.dto.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested card is not found.
 * This exception maps to an HTTP 404 Not Found status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CardNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs a new CardNotFoundException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     */
    public CardNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new CardNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     *              (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public CardNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}