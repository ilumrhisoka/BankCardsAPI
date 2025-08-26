package com.example.bankcards.exception.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Generic DTO for error responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> errors;

    /**
     * Constructs a new ErrorResponse with the given HTTP status, message, and request path.
     * The timestamp is set to the current time, and the error phrase is derived from the HttpStatus.
     *
     * @param status the HTTP status of the error.
     * @param message a descriptive error message.
     * @param path the request URI path.
     */
    public ErrorResponse(HttpStatus status, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    /**
     * Constructs a new ErrorResponse with the given HTTP status, message, request path, and a map of specific errors.
     * This constructor is particularly useful for validation errors where multiple fields might have issues.
     * The timestamp is set to the current time, and the error phrase is derived from the HttpStatus.
     *
     * @param status the HTTP status of the error.
     * @param message a general descriptive error message.
     * @param path the request URI path.
     * @param errors a map containing specific field errors (e.g., field name to error message).
     */
    public ErrorResponse(HttpStatus status, String message, String path, Map<String, String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
        this.errors = errors;
    }
}