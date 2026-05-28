package org.example.exception;

import org.example.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Помилки валідації DTO (@Valid). Збираємо всі поля у мапу.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse body = ErrorResponse.withDetails(
                "VALIDATION_ERROR",
                "Помилка валідації вхідних даних",
                HttpStatus.BAD_REQUEST.value(),
                details
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        ErrorResponse body = ErrorResponse.of(
                "EMAIL_ALREADY_EXISTS", ex.getMessage(), HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EndpointAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEndpointExists(EndpointAlreadyExistsException ex) {
        ErrorResponse body = ErrorResponse.of(
                "ENDPOINT_ALREADY_EXISTS", ex.getMessage(), HttpStatus.CONFLICT.value()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EndpointNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEndpointNotFound(EndpointNotFoundException ex) {
        ErrorResponse body = ErrorResponse.of(
                "ENDPOINT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(Exception ex) {
        ErrorResponse body = ErrorResponse.of(
                "INVALID_CREDENTIALS", "Невірний email або пароль", HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Загальний обробник - щоб клієнт не бачив стектрейс.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse body = ErrorResponse.of(
                "INTERNAL_ERROR", "Внутрішня помилка сервера", HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}