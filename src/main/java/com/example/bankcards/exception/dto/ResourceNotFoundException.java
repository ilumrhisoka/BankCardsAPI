package com.example.bankcards.exception.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for resource not found errors, resulting in an HTTP 404 Not Found status.
 * This indicates that the requested resource could not be found on the server.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}