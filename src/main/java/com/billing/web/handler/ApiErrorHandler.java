package com.billing.web.handler;

import com.billing.exception.BillingException;
import com.billing.exception.ConfigurationException;
import com.billing.exception.InvalidRequestException;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ServiceTypeUnitMismatchException;
import com.billing.web.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

    @ExceptionHandler(ServiceTypeUnitMismatchException.class)
    public ResponseEntity<ErrorResponse> handleServiceTypeUnitMismatch(
            ServiceTypeUnitMismatchException ex,
            HttpServletRequest request) {
        log.warn("Service type and unit mismatch on {}: {}", request.getRequestURI(), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ApiErrorMessages.SERVICE_TYPE_UNIT_MISMATCH, request);
    }

    @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Invalid request on {}: {}", request.getRequestURI(), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ApiErrorMessages.INVALID_REQUEST, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.debug("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, ApiErrorMessages.NOT_FOUND, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.debug("No handler for {}: {}", request.getRequestURI(), ex.getMessage());
        return respond(HttpStatus.NOT_FOUND, ApiErrorMessages.NOT_FOUND, request);
    }

    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleConfiguration(ConfigurationException ex, HttpServletRequest request) {
        log.error("Configuration error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorMessages.CONFIGURATION_ERROR, request);
    }

    @ExceptionHandler({BillingException.class, IllegalStateException.class, Exception.class})
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex, HttpServletRequest request) {
        if (ex instanceof BillingException || ex instanceof IllegalStateException) {
            log.error("Billing error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        }
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorMessages.INTERNAL_ERROR, request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            String details = validationEx.getBindingResult().getFieldErrors().stream()
                    .map(ApiErrorHandler::formatFieldError)
                    .collect(Collectors.joining(", "));
            log.warn("Request body validation failed on {}: {}", request.getRequestURI(), details);
        } else {
            log.warn("Constraint violation on {}: {}", request.getRequestURI(), ex.getMessage());
        }
        return respond(HttpStatus.BAD_REQUEST, ApiErrorMessages.VALIDATION_FAILED, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        log.warn("Missing request parameter on {}: {}", request.getRequestURI(), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ApiErrorMessages.MISSING_PARAMETER, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        log.warn("Invalid parameter type on {}: {}={}", request.getRequestURI(), ex.getName(), ex.getValue());
        return respond(HttpStatus.BAD_REQUEST, ApiErrorMessages.INVALID_PARAMETER, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Unreadable request body on {}: {}", request.getRequestURI(), ex.getMessage());
        return respond(HttpStatus.BAD_REQUEST, ApiErrorMessages.MALFORMED_BODY, request);
    }

    private static String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private static ResponseEntity<ErrorResponse> respond(
            HttpStatus status,
            String clientMessage,
            HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                System.currentTimeMillis(),
                status.value(),
                status.getReasonPhrase(),
                clientMessage,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
