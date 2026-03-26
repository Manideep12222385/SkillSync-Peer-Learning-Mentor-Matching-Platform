package com.skillsync.session.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRuntime(RuntimeException ex) {

        return Map.of(
                "timestamp", LocalDateTime.now(),
                "error", "BUSINESS_ERROR",
                "message", ex.getMessage()
        );
    }
}