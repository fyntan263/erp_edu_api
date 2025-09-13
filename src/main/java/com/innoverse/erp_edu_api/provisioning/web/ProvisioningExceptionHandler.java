package com.innoverse.erp_edu_api.provisioning.web;

import com.innoverse.erp_edu_api.common.errors.DomainException;
import com.innoverse.erp_edu_api.common.errors.ApiError;
import com.innoverse.erp_edu_api.common.errors.FieldValidationError;
import com.innoverse.erp_edu_api.provisioning.exceptions.ProvisioningException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "com.innoverse.erp_edu_api.provisioning.api.web")
public class ProvisioningExceptionHandler {

    @ExceptionHandler(ProvisioningException.class)
    public ResponseEntity<ApiError> handleProvisioningException(
            DomainException ex, HttpServletRequest request) {

        log.error("Provisioning error: {} - {}", ex.getCode(), ex.getMessage(), ex);

        ApiError apiError = ApiError.builder()
                .code(ex.getCode())
                .error(ex.getStatus().getReasonPhrase())
                .message(ex.getClientMessage())
                .status(ex.getStatus().value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .debugMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(apiError, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldValidationError(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .code("VALIDATION_001")
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed for the provided data")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<FieldValidationError> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> new FieldValidationError(
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue(),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .code("VALIDATION_002")
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Constraint violation occurred")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
                .code("VALIDATION_003")
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Malformed JSON request")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .debugMessage(ex.getMostSpecificCause().getMessage())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
                .code("VALIDATION_004")
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Invalid argument provided")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .debugMessage(ex.getMessage())
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiError apiError = ApiError.builder()
                .code("INTERNAL_001")
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .debugMessage("Please contact support with this reference")
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiError> handleHttpMediaTypeNotAcceptableException(
            org.springframework.web.HttpMediaTypeNotAcceptableException ex, HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
                .code("MEDIA_TYPE_001")
                .error(HttpStatus.NOT_ACCEPTABLE.getReasonPhrase())
                .message("Requested media type is not acceptable")
                .status(HttpStatus.NOT_ACCEPTABLE.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .debugMessage("Accept header contains unsupported media types")
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.NOT_ACCEPTABLE);
    }
}