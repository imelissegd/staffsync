package com.mgd.employee_mgmt.exception;

import com.mgd.employee_mgmt.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final MessageUtil msg;

    @Autowired
    public GlobalExceptionHandler(MessageUtil msg) {
        this.msg = msg;
    }

    // Handles NoSuchElementException — entity not found by ID or name
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handles IllegalArgumentException — duplicate ID, invalid salary, bad input
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handles IllegalStateException — e.g. deleting a department with employees
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.CONFLICT);
    }

    // Handles @Valid annotation failures on request body fields
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse(msg.get("error.validation.failed"));
        return buildResponse(false, message, HttpStatus.BAD_REQUEST);
    }

    // Handles invalid login credentials
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(InvalidCredentialsException ex) {
        return buildResponse(false, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // Catch-all — any other unexpected exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(false,
                msg.get("error.unexpected", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(boolean success, String message,
                                                               HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
