package org.example.homework.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.homework.dto.response.ErrorResponse;
import org.example.homework.dto.response.ValidationDetail;
import org.example.homework.exception.BusinessException;
import org.example.homework.exception.DuplicateException;
import org.example.homework.exception.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Business rule violation: {}", ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex instanceof DuplicateException) {
            status = HttpStatus.CONFLICT;
        }
        return build(status, ex.getMessage(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        List<ValidationDetail> details = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ValidationDetail(e.getField(), e.getDefaultMessage()))
            .toList();

        ErrorResponse body = ErrorResponse.builder()
            .status(400)
            .error("Validation Failed")
            .message("Input validation failed")
            .path(getPath(request))
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .details(details)
            .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, WebRequest request) {
        log.error("Unexpected error at {}: {}", getPath(request), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // === helpers ===

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(ErrorResponse.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(getPath(request))
            .timestamp(LocalDateTime.now(ZoneOffset.UTC))
            .build());
    }

    private String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            return swr.getRequest().getRequestURI();
        }
        return "unknown";
    }
}
