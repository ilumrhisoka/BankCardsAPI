package com.example.bankcards.exception.user;

import com.example.bankcards.exception.dto.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an attempt is made to create a user with a username that already exists.
 * This exception maps to an HTTP 409 Conflict status.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateUsernameException extends ConflictException {

    /**
     * Constructs a new DuplicateUsernameException with the specified detail message.
     *
     * @param message the detail message.
     */
    public DuplicateUsernameException(String message) {
        super(message);
    }

    /**
     * Constructs a new DuplicateUsernameException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DuplicateUsernameException(String message, Throwable cause) {
        super(message, cause);
    }
}