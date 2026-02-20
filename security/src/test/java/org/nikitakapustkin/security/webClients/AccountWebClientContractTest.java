package org.nikitakapustkin.security.webClients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikitakapustkin.bank.contracts.dto.response.AccountDetailsResponseDto;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.security.adapters.out.bank.AccountWebClient;
import org.nikitakapustkin.security.adapters.out.bank.BankWebClientSupport;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.DepositRequestDto;
import org.nikitakapustkin.security.dto.TransferRequestDto;
import org.nikitakapustkin.security.enums.ErrorCode;
import org.nikitakapustkin.security.exceptions.BankServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

class AccountWebClientContractTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private MockWebServer server;
  private AccountWebClient accountWebClient;

  @BeforeEach
  void setUp() {
    server = new MockWebServer();
    try {
      server.start();
    } catch (Exception e) {
      Assumptions.assumeTrue(false, "MockWebServer not available: " + e.getMessage());
    }
    WebClient webClient =
        WebClient.builder()
            .baseUrl(server.url("/").toString())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    accountWebClient = new AccountWebClient(webClient, new BankWebClientSupport());
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  void create_account_sends_owner_id_and_maps_response() throws Exception {
    UUID ownerId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto responseBody =
        new org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto(
            accountId, ownerId, new BigDecimal("0.00"));
    server.enqueue(
        new MockResponse()
            .setBody(objectMapper.writeValueAsString(responseBody))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    AccountResponseDto created = accountWebClient.createAccount(ownerId);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/accounts");

    Map<String, Object> payload =
        objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
    assertThat(payload.get("ownerId").toString()).isEqualTo(ownerId.toString());
    assertThat(created.getAccountId()).isEqualTo(accountId);
    assertThat(created.getOwnerId()).isEqualTo(ownerId);
    assertThat(created.getBalance()).isEqualByComparingTo("0.00");
  }

  @Test
  void deposit_sends_amount_wrapper() throws Exception {
    UUID accountId = UUID.randomUUID();
    server.enqueue(new MockResponse().setResponseCode(200));

    accountWebClient.deposit(accountId, new DepositRequestDto(new BigDecimal("10.00")));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/accounts/" + accountId + "/deposit");

    Map<String, Object> payload =
        objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
    assertThat(new BigDecimal(payload.get("amount").toString())).isEqualByComparingTo("10.00");
  }

  @Test
  void transfer_sends_amount_wrapper_and_to_account_id() throws Exception {
    UUID accountId = UUID.randomUUID();
    UUID toAccountId = UUID.randomUUID();
    server.enqueue(new MockResponse().setResponseCode(200));

    accountWebClient.transfer(
        accountId, new TransferRequestDto(toAccountId, new BigDecimal("15.50")));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/accounts/" + accountId + "/transfer");

    Map<String, Object> payload =
        objectMapper.readValue(request.getBody().readUtf8(), new TypeReference<>() {});
    assertThat(payload.get("toAccountId").toString()).isEqualTo(toAccountId.toString());
    assertThat(new BigDecimal(payload.get("amount").toString())).isEqualByComparingTo("15.50");
  }

  @Test
  void get_account_maps_details_response() throws Exception {
    UUID accountId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    UUID txId = UUID.randomUUID();
    AccountDetailsResponseDto details = getAccountDetailsResponseDto(accountId, ownerId, txId);

    server.enqueue(
        new MockResponse()
            .setBody(objectMapper.writeValueAsString(details))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    AccountResponseDto result = accountWebClient.getAccountById(accountId);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/accounts/" + accountId);

    assertThat(result.getAccountId()).isEqualTo(accountId);
    assertThat(result.getOwnerId()).isEqualTo(ownerId);
    assertThat(result.getTransactions()).hasSize(1);
    assertThat(result.getTransactions().get(0).getTransactionId()).isEqualTo(txId);
    assertThat(result.getTransactions().get(0).getTransactionType())
        .isEqualTo(TransactionType.DEPOSIT);
  }

  @NotNull
  private static AccountDetailsResponseDto getAccountDetailsResponseDto(
      UUID accountId, UUID ownerId, UUID txId) {
    org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto account =
        new org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto(
            accountId, ownerId, new BigDecimal("10.00"));
    org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto tx =
        new org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto(
            txId,
            accountId,
            TransactionType.DEPOSIT,
            new BigDecimal("10.00"),
            Instant.parse("2024-01-01T00:00:00Z"));
    return new AccountDetailsResponseDto(account, List.of(tx));
  }

  @Test
  void get_account_propagates_bank_error() throws Exception {
    UUID accountId = UUID.randomUUID();
    org.nikitakapustkin.bank.contracts.errors.ApiError error =
        new org.nikitakapustkin.bank.contracts.errors.ApiError(
            Instant.parse("2024-01-01T00:00:00Z"),
            404,
            org.nikitakapustkin.bank.contracts.errors.ErrorCode.NOT_FOUND,
            "Account not found",
            "/accounts/" + accountId);
    server.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .setBody(objectMapper.writeValueAsString(error))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    assertThatThrownBy(() -> accountWebClient.getAccountById(accountId))
        .isInstanceOf(BankServiceException.class)
        .satisfies(
            ex -> {
              BankServiceException bankEx = (BankServiceException) ex;
              assertThat(bankEx.getStatus()).isEqualTo(404);
              assertThat(bankEx.getError()).isEqualTo(ErrorCode.NOT_FOUND);
              assertThat(bankEx.getMessage()).isEqualTo("Account not found");
            });

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/accounts/" + accountId);
  }

  @Test
  void deposit_falls_back_to_status_mapping_when_error_body_invalid() {
    UUID accountId = UUID.randomUUID();
    server.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setBody("not-json")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    assertThatThrownBy(
            () ->
                accountWebClient.deposit(accountId, new DepositRequestDto(new BigDecimal("10.00"))))
        .isInstanceOf(BankServiceException.class)
        .satisfies(
            ex -> {
              BankServiceException bankEx = (BankServiceException) ex;
              assertThat(bankEx.getStatus()).isEqualTo(400);
              assertThat(bankEx.getError()).isEqualTo(ErrorCode.INVALID_ARGUMENT);
              assertThat(bankEx.getMessage()).isEqualTo("Error from main service");
            });
  }
}
