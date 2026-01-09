package ru.astondevs.learn.vorobev.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmailException (DuplicateEmailException ex) {
        Map<String, Object> errorDetails = new HashMap<>();

        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.CONFLICT.value());
        errorDetails.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("type", "DUPLICATE_EMAIL");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException (ResourceNotFoundException ex) {
        Map<String, Object> errorDetails = new HashMap<>();

        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.NOT_FOUND.value());
        errorDetails.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        errorDetails.put("message", ex.getMessage());
        errorDetails.put("type", "RESOURCE_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Validation failed");
        errorDetails.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        Map<String, String> violations = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            violations.put(fieldName, message);
        });

        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", "Constraint violation");
        errorDetails.put("violations", violations);

        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> errorDetails = new HashMap<>();

        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDetails.put("error", "Internal server error");
        errorDetails.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }
}
