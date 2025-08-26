package com.example.bankcards.exception.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Base exception for client-side errors that result in an HTTP 400 Bad Request status.
 * This typically indicates that the client has sent an invalid request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    /**
     * Constructs a new BadRequestException with the specified detail message.
     *
     * @param message the detail message.
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new BadRequestException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}