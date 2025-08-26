package com.example.bankcards.exception.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for forbidden access errors, resulting in an HTTP 403 Forbidden status.
 * This indicates that the server understood the request but refuses to authorize it.
 * This is typically used when a user is authenticated but does not have the necessary permissions.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {

    /**
     * Constructs a new ForbiddenException with the specified detail message.
     *
     * @param message the detail message.
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Constructs a new ForbiddenException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}