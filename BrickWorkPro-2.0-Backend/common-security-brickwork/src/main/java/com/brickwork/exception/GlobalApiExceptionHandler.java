package com.brickwork.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        log.warn("API error {}: {} path={}", ex.getStatusCode(), message, request.getRequestURI());
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiErrorResponse.of(ex.getStatusCode(), message, message, request.getRequestURI()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiErrorResponse> handleIOException(IOException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Failed to process file upload";
        log.warn("IO error: {} path={}", message, request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message, message, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        log.warn("Bad request: {} path={}", message, request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(HttpStatus.BAD_REQUEST.value(), message, message, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        int status = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        log.warn("Request failed with status {}: {} path={}", status, message, request.getRequestURI());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiErrorResponse.of(status, message, message, request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed path={}", request.getRequestURI());
        String message = "Invalid username or password";
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), message, message, request.getRequestURI()));
    }

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ResponseEntity<ApiErrorResponse> handleNoFallback(NoFallbackAvailableException ex, HttpServletRequest request) {
        String message = resolveDownstreamMessage(ex.getCause());
        log.error("Downstream service unavailable path={}", request.getRequestURI(), ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        message,
                        message,
                        request.getRequestURI()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiErrorResponse> handleFeign(FeignException ex, HttpServletRequest request) {
        int status = ex.status() > 0 ? ex.status() : HttpStatus.SERVICE_UNAVAILABLE.value();
        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            status = httpStatus.value();
        }
        String message = ex.getMessage() != null ? ex.getMessage() : "Downstream service call failed";
        log.warn("Feign call failed status={} path={}", status, request.getRequestURI());
        return ResponseEntity
                .status(httpStatus)
                .body(ApiErrorResponse.of(status, message, message, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on path={}", request.getRequestURI(), ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        message,
                        message,
                        request.getRequestURI()));
    }

    private static String resolveDownstreamMessage(Throwable cause) {
        if (cause instanceof TimeoutException) {
            return "A required service timed out. Please try again.";
        }
        if (cause != null && cause.getMessage() != null && cause.getMessage().contains("Connect timed out")) {
            return "A required service is unreachable. Ensure all backend services are running.";
        }
        return "A required service is temporarily unavailable. Please try again.";
    }
}