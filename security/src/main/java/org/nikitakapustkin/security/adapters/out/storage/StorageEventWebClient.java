package org.nikitakapustkin.security.adapters.out.storage;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.ports.out.StorageEventClientPort;
import org.nikitakapustkin.security.dto.StorageEventResponseDto;
import org.nikitakapustkin.security.enums.ErrorCode;
import org.nikitakapustkin.security.exceptions.BankServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
@RequiredArgsConstructor
public class StorageEventWebClient implements StorageEventClientPort {
  private final WebClient.Builder webClientBuilder;

  @Value("${storage.base-url:http://localhost:9093}")
  private String storageBaseUrl;

  @Value("${storage.webclient.connect-timeout-ms:2000}")
  private int connectTimeoutMs;

  @Value("${storage.webclient.response-timeout-ms:5000}")
  private int responseTimeoutMs;

  @Override
  public List<StorageEventResponseDto> getEvents(
      String source,
      String eventType,
      UUID entityId,
      UUID correlationId,
      String transactionType,
      Integer limit) {
    WebClient storageClient = buildStorageClient();
    Mono<List<StorageEventResponseDto>> response =
        storageClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path("/events")
                        .queryParamIfPresent("source", Optional.ofNullable(source))
                        .queryParamIfPresent("eventType", Optional.ofNullable(eventType))
                        .queryParamIfPresent("entityId", Optional.ofNullable(entityId))
                        .queryParamIfPresent("correlationId", Optional.ofNullable(correlationId))
                        .queryParamIfPresent(
                            "transactionType", Optional.ofNullable(transactionType))
                        .queryParamIfPresent("limit", Optional.ofNullable(limit))
                        .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::toStorageException)
            .bodyToMono(new ParameterizedTypeReference<>() {});
    List<StorageEventResponseDto> events = response.block();
    return events == null ? List.of() : events;
  }

  private WebClient buildStorageClient() {
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(responseTimeoutMs));

    return webClientBuilder
        .clone()
        .baseUrl(storageBaseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  private Mono<? extends Throwable> toStorageException(
      org.springframework.web.reactive.function.client.ClientResponse response) {
    int status = response.statusCode().value();
    return response
        .bodyToMono(String.class)
        .defaultIfEmpty("")
        .map(
            body ->
                new BankServiceException(
                    status, mapErrorCode(status), resolveMessage(body, status), "/events"));
  }

  private static ErrorCode mapErrorCode(int status) {
    return switch (status) {
      case 400 -> ErrorCode.INVALID_ARGUMENT;
      case 401 -> ErrorCode.UNAUTHORIZED;
      case 403 -> ErrorCode.FORBIDDEN;
      case 404 -> ErrorCode.NOT_FOUND;
      default -> ErrorCode.INTERNAL_ERROR;
    };
  }

  private static String resolveMessage(String body, int status) {
    if (body != null && !body.isBlank()) {
      return "Storage service error [" + status + "]: " + body;
    }
    return "Storage service error [" + status + "]";
  }
}
