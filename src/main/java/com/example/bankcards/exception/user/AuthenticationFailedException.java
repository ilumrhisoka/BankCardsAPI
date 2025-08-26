package com.example.bankcards.exception.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user authentication fails, e.g., due to incorrect credentials.
 * This exception maps to an HTTP 401 Unauthorized status.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationFailedException extends RuntimeException {

    /**
     * Constructs a new AuthenticationFailedException with the specified detail message.
     *
     * @param message the detail message.
     */
    public AuthenticationFailedException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuthenticationFailedException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}