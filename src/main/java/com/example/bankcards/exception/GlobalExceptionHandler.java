package com.example.bankcards.exception;

import com.example.bankcards.exception.card.CardStatusException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.dto.BadRequestException;
import com.example.bankcards.exception.dto.ConflictException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.exception.dto.ErrorResponse;
import com.example.bankcards.exception.transfer.InvalidTransferException;
import com.example.bankcards.exception.user.AuthenticationFailedException;
import com.example.bankcards.exception.user.DuplicateUsernameException;
import com.example.bankcards.exception.user.TokenExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.bankcards.exception.card.CardOwnershipException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application.
 * This class intercepts exceptions thrown by controllers and provides a centralized
 * way to handle them, returning consistent {@link ErrorResponse} objects.
 * It is annotated with {@link RestControllerAdvice} to apply to all controllers.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Helper method to create a {@link ResponseEntity} containing an {@link ErrorResponse}.
     * Reduces code duplication in exception handlers.
     *
     * @param status the HTTP status to set in the response.
     * @param message the error message to include.
     * @param request the current {@link HttpServletRequest} to extract the request URI.
     * @param errors an optional map of field-specific errors, typically for validation failures.
     * @return a {@link ResponseEntity} with the constructed {@link ErrorResponse} and the specified HTTP status.
     */
    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String message, HttpServletRequest request, Map<String, String> errors) {
        ErrorResponse errorResponse = new ErrorResponse(status, message, request.getRequestURI(), errors);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Overloaded helper method to create a {@link ResponseEntity} containing an {@link ErrorResponse}
     * without specific field errors.
     *
     * @param status the HTTP status to set in the response.
     * @param message the error message to include.
     * @param request the current {@link HttpServletRequest} to extract the request URI.
     * @return a {@link ResponseEntity} with the constructed {@link ErrorResponse} and the specified HTTP status.
     */
    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String message, HttpServletRequest request) {
        return createErrorResponseEntity(status, message, request, null);
    }

    /**
     * Handles {@link CardOwnershipException}, which occurs when a user attempts an operation
     * on a card they do not own.
     * Returns an HTTP 403 Forbidden status.
     *
     * @param ex the {@link CardOwnershipException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 403 Forbidden status.
     */
    @ExceptionHandler(CardOwnershipException.class)
    public ResponseEntity<ErrorResponse> handleCardOwnershipException(CardOwnershipException ex, HttpServletRequest request) {
        log.error("CardOwnershipException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage(), request);
    }

    /**
     * Handles general {@link ServletException}s that may occur during request processing.
     * Returns an HTTP 500 Internal Server Error status.
     *
     * @param ex the {@link ServletException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ErrorResponse> handleServletException(ServletException ex, HttpServletRequest request) {
        log.error("ServletException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Servlet error: " + ex.getMessage(), request);
    }

    /**
     * Handles {@link IOException}s that may occur during I/O operations.
     * Returns an HTTP 500 Internal Server Error status.
     *
     * @param ex the {@link IOException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex, HttpServletRequest request) {
        log.error("IOException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "I/O error: " + ex.getMessage(), request);
    }

    /**
     * Handles {@link ResourceNotFoundException} and its subclasses.
     * Returns an HTTP 404 Not Found status.
     *
     * @param ex the {@link ResourceNotFoundException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 404 Not Found status.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("ResourceNotFoundException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles {@link BadRequestException} and its subclasses (e.g., {@link CardStatusException}, {@link InsufficientFundsException},
     * {@link InvalidTransferException}).
     * Returns an HTTP 400 Bad Request status.
     *
     * @param ex the {@link BadRequestException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 400 Bad Request status.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.error("BadRequestException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles {@link ConflictException} and its subclasses (e.g., {@link DuplicateUsernameException}).
     * Returns an HTTP 409 Conflict status.
     *
     * @param ex the {@link ConflictException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 409 Conflict status.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex, HttpServletRequest request) {
        log.error("ConflictException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles {@link ForbiddenException} and its subclasses (e.g., {@link TokenExpiredException}).
     * Returns an HTTP 403 Forbidden status.
     *
     * @param ex the {@link ForbiddenException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 403 Forbidden status.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        log.error("ForbiddenException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handles {@link AuthenticationFailedException}, which occurs when user authentication fails.
     * Returns an HTTP 401 Unauthorized status.
     *
     * @param ex the {@link AuthenticationFailedException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 401 Unauthorized status.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException ex, HttpServletRequest request) {
        log.error("AuthenticationFailedException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Handles {@link TokenExpiredException}, which occurs when an authentication token is expired or invalid.
     * Returns an HTTP 403 Forbidden status.
     *
     * @param ex the {@link TokenExpiredException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 403 Forbidden status.
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex, HttpServletRequest request) {
        log.error("TokenExpiredException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Handles {@link MethodArgumentNotValidException}, typically thrown when method arguments
     * annotated with {@code @Valid} fail validation.
     * Returns an HTTP 400 Bad Request status and includes details of validation errors.
     *
     * @param ex the {@link MethodArgumentNotValidException} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 400 Bad Request status,
     *         including a map of validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("MethodArgumentNotValidException: {}", ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return createErrorResponseEntity(HttpStatus.BAD_REQUEST, "Validation failed", request, errors);
    }

    /**
     * Global handler for all other unexpected exceptions not specifically handled by other methods.
     * This acts as a fallback mechanism to ensure that any unhandled exception
     * is caught and returned in a structured {@link ErrorResponse} format.
     * Returns an HTTP 500 Internal Server Error status.
     *
     * @param ex the {@link Exception} that was thrown.
     * @param request the current {@link HttpServletRequest}.
     * @return a {@link ResponseEntity} with an {@link ErrorResponse} and HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), request);
    }
}