package com.example.bankcards.exception.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for resource conflicts, typically resulting in an HTTP 409 Conflict status.
 * This indicates that the request could not be completed due to a conflict with the current state of the target resource.
 * For example, attempting to create a resource that already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    /**
     * Constructs a new ConflictException with the specified detail message.
     *
     * @param message the detail message.
     */
    public ConflictException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConflictException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}