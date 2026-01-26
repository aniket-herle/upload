package com.aniket.mirror.upload.exception;

import com.aniket.mirror.common.ApiErrorResponse;
import com.aniket.mirror.common.ValidationError;
import com.aniket.mirror.common.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        String traceId = MDC.get("traceId");
        List<ValidationError> details = null;

        if (status.is4xxClientError()) {
            log.warn("Client error: {} - {}", ex.getErrorCode(), ex.getMessage());
        } else {
            log.error("Server error: {} - {}", ex.getErrorCode(), ex.getMessage(), ex);
        }

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                ex.getErrorCode().name(),
                details
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String traceId = MDC.get("traceId");
        List<ValidationError> details = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(new ValidationError(error.getField(), error.getDefaultMessage()));
        }

        log.warn("Validation error: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                traceId,
                "INVALID_INPUT",
                details
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String traceId = MDC.get("traceId");
        List<ValidationError> details = new ArrayList<>();

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String field = violation.getPropertyPath().toString();
            details.add(new ValidationError(field, violation.getMessage()));
        }

        log.warn("Constraint violation: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Constraint violation",
                request.getRequestURI(),
                traceId,
                "INVALID_INPUT",
                details
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String traceId = MDC.get("traceId");

        log.warn("Malformed request: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Malformed request body",
                request.getRequestURI(),
                traceId,
                "INVALID_INPUT",
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String traceId = MDC.get("traceId");

        log.warn("Missing parameter: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                traceId,
                "INVALID_INPUT",
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String traceId = MDC.get("traceId");

        log.warn("Type mismatch: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Invalid parameter type",
                request.getRequestURI(),
                traceId,
                "INVALID_INPUT",
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        String traceId = MDC.get("traceId");

        log.warn("Data integrity violation: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Data integrity violation",
                request.getRequestURI(),
                traceId,
                "CONFLICT",
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String traceId = MDC.get("traceId");

        log.warn("No handler found: {}", ex.getMessage());

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Resource not found",
                request.getRequestURI(),
                traceId,
                "RESOURCE_NOT_FOUND",
                null
        );

        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String traceId = MDC.get("traceId");

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ApiErrorResponse response = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Internal server error",
                request.getRequestURI(),
                traceId,
                "INTERNAL_ERROR",
                null
        );

        return new ResponseEntity<>(response, status);
    }
}