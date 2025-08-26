package com.example.bankcards.exception.transfer;

import com.example.bankcards.exception.dto.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a money transfer operation is invalid due to specific business rules,
 * such as attempting to transfer to the same card.
 * This exception maps to an HTTP 400 Bad Request status.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTransferException extends BadRequestException {

    /**
     * Constructs a new InvalidTransferException with the specified detail message.
     *
     * @param message the detail message.
     */
    public InvalidTransferException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTransferException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public InvalidTransferException(String message, Throwable cause) {
        super(message, cause);
    }
}