package org.nikitakapustkin.security.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.nikitakapustkin.security.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(NotEnoughMoneyException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughMoneyException(NotEnoughMoneyException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.NOT_ENOUGH_MONEY, ex.getMessage(), req);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ErrorCode.ALREADY_EXISTS, ex.getMessage(), req);
    }

    @ExceptionHandler(UserHasNoFriendsException.class)
    public ResponseEntity<ErrorResponse> handleUserHasNoFriendsException(UserHasNoFriendsException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation error";
        }
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, message, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation error";
        }
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, message, req);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMessageConversionException.class})
    public ResponseEntity<ErrorResponse> handleHttpMessageConversion(HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, "Malformed request body", req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, ex.getMessage(), req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(AuthenticationFailedException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, "Access denied", req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Access denied"
                : ex.getMessage();
        return build(HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN, message, req);
    }

    @ExceptionHandler(BankServiceException.class)
    public ResponseEntity<ErrorResponse> handleBankServiceException(BankServiceException ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.resolve(ex.getStatus());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorCode code = ex.getError() != null ? ex.getError() : ErrorCode.INTERNAL_ERROR;
        String message = ex.getMessage() != null ? ex.getMessage() : "Error from main service";
        return build(status, code, message, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unhandled exception (id={})", errorId, ex);
        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Internal server error (id=" + errorId + ")",
                req
        );
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode error, String message, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                req != null ? req.getRequestURI() : ""
        );
        return ResponseEntity.status(status).body(body);
    }
}
