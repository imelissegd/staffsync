package com.mgd.employee_mgmt.exception;

import com.mgd.employee_mgmt.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles NoSuchElementException — e.g. employee not found by ID
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handles IllegalArgumentException — e.g. duplicate employee ID, invalid salary
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handles @Valid annotation failures on request body fields
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return buildResponse(false, message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(InvalidCredentialsException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // Catch-all — any other unexpected exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(false, "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Shared response builder
    private ResponseEntity<Map<String, Object>> buildResponse(boolean success, String message,
                                                              HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}