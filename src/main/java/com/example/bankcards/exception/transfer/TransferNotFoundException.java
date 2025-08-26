package com.example.bankcards.exception.transfer;

import com.example.bankcards.exception.dto.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested transfer record is not found.
 * This exception maps to an HTTP 404 Not Found status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TransferNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs a new TransferNotFoundException with the specified detail message.
     *
     * @param message the detail message.
     */
    public TransferNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new TransferNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public TransferNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}