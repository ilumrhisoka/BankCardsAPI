package com.example.bankcards.exception.user;

import com.example.bankcards.exception.dto.ForbiddenException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an authentication token is expired or otherwise invalid.
 * This typically results in an HTTP 403 Forbidden status, indicating that the request
 * cannot be processed due to an invalid or expired credential.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenExpiredException extends ForbiddenException {

    /**
     * Constructs a new TokenExpiredException with the specified detail message.
     *
     * @param message the detail message.
     */
    public TokenExpiredException(String message) {
        super(message);
    }

    /**
     * Constructs a new TokenExpiredException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}