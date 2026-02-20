package org.nikitakapustkin.security.webClients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikitakapustkin.security.adapters.out.bank.BankWebClientSupport;
import org.nikitakapustkin.security.adapters.out.bank.UserWebClient;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;
import org.nikitakapustkin.security.dto.UserResponseDto;
import org.nikitakapustkin.security.enums.ErrorCode;
import org.nikitakapustkin.security.exceptions.BankServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

class UserWebClientContractTest {

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
  private MockWebServer server;
  private UserWebClient userWebClient;

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
    userWebClient = new UserWebClient(webClient, new BankWebClientSupport());
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  void get_users_maps_response() throws Exception {
    UUID userId = UUID.randomUUID();
    org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto response =
        new org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto(
            userId,
            "alice",
            "Alice",
            25,
            org.nikitakapustkin.bank.contracts.enums.Sex.FEMALE,
            org.nikitakapustkin.bank.contracts.enums.HairColor.BLACK,
            List.of("bob"));
    String json = objectMapper.writeValueAsString(List.of(response));
    server.enqueue(
        new MockResponse()
            .setBody(json)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    List<UserResponseDto> result = userWebClient.getUsers("BLACK", "FEMALE");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/users?hairColor=BLACK&sex=FEMALE");

    assertThat(result).hasSize(1);
    UserResponseDto dto = result.get(0);
    assertThat(dto.getId()).isEqualTo(userId);
    assertThat(dto.getLogin()).isEqualTo("alice");
    assertThat(dto.getFriendsLogins()).containsExactly("bob");
  }

  @Test
  void get_user_info_maps_details_response() throws Exception {
    UUID userId = UUID.randomUUID();
    org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto user =
        new org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto(
            userId,
            "alice",
            "Alice",
            25,
            org.nikitakapustkin.bank.contracts.enums.Sex.FEMALE,
            org.nikitakapustkin.bank.contracts.enums.HairColor.BLACK,
            List.of("bob"));
    org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto account =
        new org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto(
            UUID.randomUUID(), userId, new BigDecimal("10.00"));
    org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto details =
        new org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto(
            user, List.of("bob", "carol"), List.of(account));
    String json = objectMapper.writeValueAsString(details);
    server.enqueue(
        new MockResponse()
            .setBody(json)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    UserResponseDto dto = userWebClient.getUserInfo(userId);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/users/" + userId);

    assertThat(dto.getId()).isEqualTo(userId);
    assertThat(dto.getFriendsLogins()).containsExactly("bob", "carol");
    assertThat(dto.getAccounts()).hasSize(1);
    assertThat(dto.getAccounts().get(0).getOwnerId()).isEqualTo(userId);
  }

  @Test
  void create_friendship_sends_expected_payload() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(201));

    UUID userId = UUID.randomUUID();
    UUID friendId = UUID.randomUUID();
    userWebClient.createFriendship(userId, friendId);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/users/friends");

    String body = request.getBody().readUtf8();
    assertThat(body).contains("\"userId\":\"" + userId + "\"");
    assertThat(body).contains("\"friendId\":\"" + friendId + "\"");
  }

  @Test
  void create_user_propagates_bank_error() throws Exception {
    org.nikitakapustkin.bank.contracts.errors.ApiError error =
        new org.nikitakapustkin.bank.contracts.errors.ApiError(
            Instant.parse("2024-01-01T00:00:00Z"),
            409,
            org.nikitakapustkin.bank.contracts.errors.ErrorCode.ALREADY_EXISTS,
            "User already exists",
            "/users");
    server.enqueue(
        new MockResponse()
            .setResponseCode(409)
            .setBody(objectMapper.writeValueAsString(error))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    assertThatThrownBy(
            () ->
                userWebClient.sendUserCreateRequest(
                    new UserCreateRequestDto("alice", "pass", "Alice", "FEMALE", "BLACK", 25)))
        .isInstanceOf(BankServiceException.class)
        .satisfies(
            ex -> {
              BankServiceException bankEx = (BankServiceException) ex;
              assertThat(bankEx.getStatus()).isEqualTo(409);
              assertThat(bankEx.getError()).isEqualTo(ErrorCode.ALREADY_EXISTS);
              assertThat(bankEx.getMessage()).isEqualTo("User already exists");
            });
  }

  @Test
  void get_user_info_falls_back_to_status_mapping_when_error_code_unknown() throws Exception {
    UUID userId = UUID.randomUUID();
    org.nikitakapustkin.bank.contracts.errors.ApiError error =
        new org.nikitakapustkin.bank.contracts.errors.ApiError(
            Instant.parse("2024-01-01T00:00:00Z"), 403, null, "Forbidden", "/users/" + userId);
    server.enqueue(
        new MockResponse()
            .setResponseCode(403)
            .setBody(objectMapper.writeValueAsString(error))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    assertThatThrownBy(() -> userWebClient.getUserInfo(userId))
        .isInstanceOf(BankServiceException.class)
        .satisfies(
            ex -> {
              BankServiceException bankEx = (BankServiceException) ex;
              assertThat(bankEx.getStatus()).isEqualTo(403);
              assertThat(bankEx.getError()).isEqualTo(ErrorCode.FORBIDDEN);
              assertThat(bankEx.getMessage()).isEqualTo("Forbidden");
            });
  }
}
