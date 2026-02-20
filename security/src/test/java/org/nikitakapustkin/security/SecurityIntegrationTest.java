package org.nikitakapustkin.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.nikitakapustkin.security.adapters.out.persistence.UserRepository;
import org.nikitakapustkin.security.application.ports.out.UserRepositoryPort;
import org.nikitakapustkin.security.enums.Role;
import org.nikitakapustkin.security.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecurityIntegrationTest {

  private static final String TEST_SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
  private static final String TEST_SERVICE_SECRET = "c2VydmljZS1zZWNyZXQtMDEyMzQ1Njc4OWFiY2RlZjA=";
  private static final String TEST_SERVICE_ISSUER = "security-service";
  private static final String TEST_SERVICE_AUDIENCE = "bank-service";
  private static MockWebServer bankServer;
  private static boolean mockServerAvailable = true;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    if (bankServer == null) {
      try {
        bankServer = new MockWebServer();
        bankServer.start();
      } catch (Exception e) {
        mockServerAvailable = false;
      }
    }
    String baseUrl =
        mockServerAvailable && bankServer != null
            ? bankServer.url("/").toString()
            : "http://localhost:0";
    registry.add("bank.base-url", () -> baseUrl);
    registry.add("storage.base-url", () -> baseUrl);
    registry.add(
        "spring.datasource.url",
        () -> "jdbc:h2:mem:securitytest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
    registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
    registry.add("spring.datasource.username", () -> "sa");
    registry.add("spring.datasource.password", () -> "");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.flyway.enabled", () -> "false");
    registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    registry.add("kafka.topics.user", () -> "client-topic");
    registry.add("outbox.publisher.enabled", () -> "false");
    registry.add("jwt.secret", () -> TEST_SECRET);
    registry.add("jwt.service.secret", () -> TEST_SERVICE_SECRET);
    registry.add("jwt.service.issuer", () -> TEST_SERVICE_ISSUER);
    registry.add("jwt.service.audience", () -> TEST_SERVICE_AUDIENCE);
  }

  @Autowired MockMvc mockMvc;
  @Autowired UserRepositoryPort userRepository;
  @Autowired UserRepository userRepositoryAdapter;
  @Autowired PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void setUp() {
    org.junit.jupiter.api.Assumptions.assumeTrue(
        mockServerAvailable, "MockWebServer not available");
    userRepositoryAdapter.deleteAll();
  }

  @AfterAll
  void tearDown() throws Exception {
    if (bankServer != null) {
      bankServer.shutdown();
    }
  }

  @Test
  void register_user_creates_security_user() throws Exception {
    String body =
        """
                {
                  "login": "alice",
                  "password": "pass",
                  "name": "Alice",
                  "sex": "FEMALE",
                  "hairColor": "BLACK",
                  "age": 25
                }
                """;

    mockMvc
        .perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());

    User saved = userRepository.findUserByLogin("alice");
    assertThat(saved).isNotNull();
    assertThat(saved.getUserId()).isNotNull();
    assertThat(saved.getRole()).isEqualTo(Role.CLIENT);
  }

  @Test
  void login_and_users_me_flow() throws Exception {
    UUID userId = UUID.randomUUID();
    userRepository.save(new User(userId, "alice", passwordEncoder.encode("pass"), Role.CLIENT));

    String loginBody =
        """
                {
                  "username": "alice",
                  "password": "pass"
                }
                """;

    MvcResult loginResult =
        mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
            .andExpect(status().isOk())
            .andReturn();

    String token = loginResult.getResponse().getContentAsString();
    assertThat(token).isNotBlank();

    mockMvc
        .perform(get("/users/me"))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));

    UUID accountId = UUID.randomUUID();
    String createdAccountJson =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "id", accountId,
                "ownerId", userId,
                "balance", 0));
    bankServer.enqueue(
        new MockResponse()
            .setResponseCode(201)
            .setBody(createdAccountJson)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    mockMvc
        .perform(post("/users/me/accounts").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isCreated());

    String userJson =
        objectMapper.writeValueAsString(
            java.util.Map.of(
                "user",
                    java.util.Map.of(
                        "id", userId,
                        "login", "alice",
                        "name", "Alice",
                        "age", 25,
                        "sex", "FEMALE",
                        "hairColor", "BLACK",
                        "friendsLogins", List.of()),
                "friendsLogins", List.of(),
                "accounts", List.of()));
    bankServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(userJson)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    mockMvc
        .perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc
        .perform(post("/logout").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(result -> assertThat(result.getResponse().getStatus()).isIn(401, 403));
  }

  @Test
  void admin_can_load_events() throws Exception {
    UUID adminUserId = UUID.randomUUID();
    userRepository.save(new User(adminUserId, "admin", passwordEncoder.encode("pass"), Role.ADMIN));

    String loginBody =
        """
                {
                  "username": "admin",
                  "password": "pass"
                }
                """;

    MvcResult loginResult =
        mockMvc
            .perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
            .andExpect(status().isOk())
            .andReturn();

    String token = loginResult.getResponse().getContentAsString();
    assertThat(token).isNotBlank();

    String eventsJson =
        objectMapper.writeValueAsString(
            List.of(
                java.util.Map.ofEntries(
                    java.util.Map.entry("eventId", UUID.randomUUID()),
                    java.util.Map.entry("correlationId", UUID.randomUUID()),
                    java.util.Map.entry("source", "ACCOUNT"),
                    java.util.Map.entry("entityId", UUID.randomUUID()),
                    java.util.Map.entry("transactionId", UUID.randomUUID()),
                    java.util.Map.entry("transactionType", "TRANSFER"),
                    java.util.Map.entry("amount", 100.50),
                    java.util.Map.entry("createdAt", "2026-02-07T10:00:00Z"),
                    java.util.Map.entry("eventType", "ACCOUNT_TRANSFERRED"),
                    java.util.Map.entry("eventTime", "2026-02-07T10:00:01Z"),
                    java.util.Map.entry("eventDescription", "Transfer completed"),
                    java.util.Map.entry("payloadType", "AccountTransferredPayload"),
                    java.util.Map.entry("payload", "{\"amount\":100.50}"))));
    bankServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(eventsJson)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    mockMvc
        .perform(
            get("/events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("source", "ACCOUNT")
                .param("limit", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].source").value("ACCOUNT"))
        .andExpect(jsonPath("$[0].eventType").value("ACCOUNT_TRANSFERRED"));
  }
}
