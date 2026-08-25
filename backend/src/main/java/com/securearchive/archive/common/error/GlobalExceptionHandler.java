package com.securearchive.archive.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.securearchive.archive.auth.exception.InvalidCredentialsException;
import com.securearchive.archive.auth.exception.DuplicateResourceException;
import com.securearchive.archive.document.exception.InvalidDocumentStateException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
        InvalidCredentialsException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            401,
            "INVALID_CREDENTIALS",
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
        DuplicateResourceException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            409,
            "DUPLICATE_RESOURCE",
            exception.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            404,
            "RESOURCE_NOT_FOUND",
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
    @ExceptionHandler(InvalidDocumentStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDocumentState(
        InvalidDocumentStateException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            409,
            "INVALID_DOCUMENT_STATE",
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
        AccessDeniedException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            403,
            "FORBIDDEN",
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다");

        ApiErrorResponse response = new ApiErrorResponse(
            400,
            "VALIDATION_ERROR",
            message,
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
            400,
            "INVALID_REQUEST_BODY",
            "요청 본문의 JSON 형식이 올바르지 않습니다",
            request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
