package com.example.bankcards.exception;

import com.example.bankcards.exception.dto.BadRequestException;
import com.example.bankcards.exception.dto.ConflictException;
import com.example.bankcards.exception.dto.ForbiddenException;
import com.example.bankcards.exception.dto.ResourceNotFoundException;
import com.example.bankcards.exception.dto.ErrorResponse; // Ваш DTO для ответа об ошибках
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j; // Для логирования
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice; // Используем RestControllerAdvice для REST API

import jakarta.servlet.ServletException; // Используем jakarta.servlet.ServletException
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Вспомогательный метод для создания ResponseEntity с ErrorResponse.
     * Уменьшает дублирование кода в обработчиках исключений.
     */
    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String message, HttpServletRequest request, Map<String, String> errors) {
        ErrorResponse errorResponse = new ErrorResponse(status, message, request.getRequestURI(), errors);
        return new ResponseEntity<>(errorResponse, status);
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(HttpStatus status, String message, HttpServletRequest request) {
        return createErrorResponseEntity(status, message, request, null);
    }

    /**
     * Обработка исключений, связанных с отказом в доступе (например, Spring Security).
     * Возвращает статус 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.error("AccessDeniedException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage(), request);
    }

    /**
     * Обработка общих исключений сервлета и ввода/вывода.
     * Изменен статус на INTERNAL_SERVER_ERROR, так как UNAUTHORIZED не всегда подходит
     * для общих проблем ввода/вывода или сервлета, если они не связаны с аутентификацией.
     */
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ErrorResponse> handleServletException(ServletException ex, HttpServletRequest request) {
        log.error("ServletException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Servlet error: " + ex.getMessage(), request);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException ex, HttpServletRequest request) {
        log.error("IOException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "I/O error: " + ex.getMessage(), request);
    }

    /**
     * Обработка исключений, когда ресурс не найден.
     * Включает CardNotFoundException, так как он наследуется от ResourceNotFoundException.
     * Возвращает статус 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.error("ResourceNotFoundException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Обработка исключений, связанных с некорректными запросами.
     * Включает CardBlockedException и InsufficientFundsException, так как они наследуются от BadRequestException.
     * Возвращает статус 400 Bad Request.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.error("BadRequestException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Обработка исключений конфликтов (например, дублирование данных).
     * Возвращает статус 409 Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex, HttpServletRequest request) {
        log.error("ConflictException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Обработка исключений, связанных с запрещенными операциями.
     * Возвращает статус 403 Forbidden.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        log.error("ForbiddenException: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Обработка исключений валидации аргументов метода (например, @Valid).
     * Возвращает статус 400 Bad Request и детали ошибок валидации.
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
     * Глобальный обработчик для всех остальных, непредвиденных исключений.
     * Это "запасной" механизм, который гарантирует, что любое необработанное исключение
     * будет перехвачено и возвращено в структурированном формате.
     * Возвращает статус 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return createErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage(), request);
    }
}