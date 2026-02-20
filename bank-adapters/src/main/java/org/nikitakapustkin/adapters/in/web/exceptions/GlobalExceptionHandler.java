package org.nikitakapustkin.adapters.in.web.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.nikitakapustkin.bank.contracts.errors.ApiError;
import org.nikitakapustkin.bank.contracts.errors.ErrorCode;
import org.nikitakapustkin.domain.exceptions.AccountNotFoundException;
import org.nikitakapustkin.domain.exceptions.NotEnoughMoneyException;
import org.nikitakapustkin.domain.exceptions.UserAlreadyExistsException;
import org.nikitakapustkin.domain.exceptions.UserHasNoFriendsException;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({UserNotFoundException.class, AccountNotFoundException.class})
  public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest req) {
    return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), req);
  }

  @ExceptionHandler({UserAlreadyExistsException.class})
  public ResponseEntity<ApiError> handleAlreadyExists(
      UserAlreadyExistsException ex, HttpServletRequest req) {
    return build(HttpStatus.CONFLICT, ErrorCode.ALREADY_EXISTS, ex.getMessage(), req);
  }

  @ExceptionHandler({NotEnoughMoneyException.class})
  public ResponseEntity<ApiError> handleNotEnoughMoney(
      NotEnoughMoneyException ex, HttpServletRequest req) {
    // бизнес-конфликт (состояние аккаунта), поэтому 409
    return build(HttpStatus.CONFLICT, ErrorCode.NOT_ENOUGH_MONEY, ex.getMessage(), req);
  }

  @ExceptionHandler({OptimisticLockingFailureException.class})
  public ResponseEntity<ApiError> handleOptimisticLock(
      OptimisticLockingFailureException ex, HttpServletRequest req) {
    return build(
        HttpStatus.CONFLICT,
        ErrorCode.CONCURRENT_UPDATE,
        "Concurrent update detected. Please retry the request.",
        req);
  }

  @ExceptionHandler({UserHasNoFriendsException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
    return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, ex.getMessage(), req);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      WebRequest request) {
    String msg =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .distinct()
            .collect(Collectors.joining("; "));
    if (msg.isBlank()) {
      msg = "Validation error";
    }

    HttpServletRequest req =
        (HttpServletRequest) request.resolveReference(WebRequest.REFERENCE_REQUEST);
    ApiError body =
        new ApiError(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            ErrorCode.VALIDATION_ERROR,
            msg,
            req != null ? req.getRequestURI() : "");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleNoResourceFoundException(
      @NonNull NoResourceFoundException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      WebRequest request) {
    HttpServletRequest req =
        (HttpServletRequest) request.resolveReference(WebRequest.REFERENCE_REQUEST);
    ApiError body =
        new ApiError(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            ErrorCode.NOT_FOUND,
            "Not found",
            req != null ? req.getRequestURI() : "");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      WebRequest request) {
    HttpServletRequest req =
        (HttpServletRequest) request.resolveReference(WebRequest.REFERENCE_REQUEST);

    String msg = "Method not allowed";
    if (ex.getSupportedHttpMethods() != null && !ex.getSupportedHttpMethods().isEmpty()) {
      msg += ". Supported: " + ex.getSupportedHttpMethods();
    }

    ApiError body =
        new ApiError(
            Instant.now(),
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            ErrorCode.METHOD_NOT_ALLOWED,
            msg,
            req != null ? req.getRequestURI() : "");
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      @NonNull HttpMessageNotReadableException ex,
      @NonNull HttpHeaders headers,
      @NonNull HttpStatusCode status,
      WebRequest request) {
    HttpServletRequest req =
        (HttpServletRequest) request.resolveReference(WebRequest.REFERENCE_REQUEST);

    ApiError body =
        new ApiError(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            ErrorCode.MESSAGE_NOT_READABLE,
            "Malformed JSON request",
            req != null ? req.getRequestURI() : "");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest req) {
    String msg =
        ex.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .distinct()
            .collect(Collectors.joining("; "));
    if (msg.isBlank()) {
      msg = "Validation error";
    }
    return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, msg, req);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest req) {
    String errorId = UUID.randomUUID().toString();
    log.error("Unhandled exception (id={})", errorId, ex);

    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_ERROR,
        "Internal server error (id=" + errorId + ")",
        req);
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status, ErrorCode error, String message, HttpServletRequest req) {
    ApiError body =
        new ApiError(Instant.now(), status.value(), error, message, req.getRequestURI());
    return ResponseEntity.status(status).body(body);
  }
}
