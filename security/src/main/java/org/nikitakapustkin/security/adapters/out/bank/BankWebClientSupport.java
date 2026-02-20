package org.nikitakapustkin.security.adapters.out.bank;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import org.nikitakapustkin.bank.contracts.errors.ApiError;
import org.nikitakapustkin.security.enums.ErrorCode;
import org.nikitakapustkin.security.exceptions.BankServiceException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class BankWebClientSupport {
  private static final int MAX_RETRIES = 2;
  private static final Duration RETRY_BACKOFF = Duration.ofMillis(200);
  private static final Duration MAX_BACKOFF = Duration.ofSeconds(2);

  public Mono<? extends Throwable> toBankException(ClientResponse response) {
    int status = response.statusCode().value();
    return response
        .bodyToMono(ApiError.class)
        .defaultIfEmpty(new ApiError(null, status, null, null, null))
        .map(
            error ->
                new BankServiceException(
                    error.status() > 0 ? error.status() : status,
                    resolveErrorCode(error.error(), status),
                    resolveMessage(error.message()),
                    error.path()))
        .onErrorResume(
            ex ->
                Mono.just(
                    new BankServiceException(
                        status, resolveErrorCode(null, status), "Error from main service", null)));
  }

  public Retry retrySpec() {
    return Retry.backoff(MAX_RETRIES, RETRY_BACKOFF)
        .maxBackoff(MAX_BACKOFF)
        .filter(BankWebClientSupport::isRetryable)
        .onRetryExhaustedThrow((spec, signal) -> signal.failure());
  }

  public <T> Mono<T> maybeRetry(Mono<T> response, boolean shouldRetry) {
    return shouldRetry ? response.retryWhen(retrySpec()) : response;
  }

  private static boolean isRetryable(Throwable error) {
    if (error instanceof BankServiceException bankEx) {
      return bankEx.getStatus() >= HttpStatusCode.valueOf(500).value();
    }
    return error instanceof WebClientRequestException || error instanceof TimeoutException;
  }

  private static ErrorCode resolveErrorCode(
      org.nikitakapustkin.bank.contracts.errors.ErrorCode raw, int status) {
    if (raw != null) {
      try {
        return ErrorCode.valueOf(raw.name());
      } catch (IllegalArgumentException ignored) {
      }
    }
    return switch (status) {
      case 400 -> ErrorCode.INVALID_ARGUMENT;
      case 401 -> ErrorCode.UNAUTHORIZED;
      case 403 -> ErrorCode.FORBIDDEN;
      case 404 -> ErrorCode.NOT_FOUND;
      case 409 -> ErrorCode.IDEMPOTENCY_CONFLICT;
      default -> ErrorCode.INTERNAL_ERROR;
    };
  }

  private static String resolveMessage(String message) {
    if (message == null || message.isBlank()) {
      return "Error from main service";
    }
    return message;
  }
}
