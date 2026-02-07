package org.nikitakapustkin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nikitakapustkin.adapters.in.web.security.JwtService;
import org.nikitakapustkin.application.ports.in.ImportUserUseCase;
import org.nikitakapustkin.application.ports.in.commands.ImportUserCommand;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BankServiceApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:banktest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.show-sql=false",
        "spring.flyway.enabled=true",
        "outbox.publisher.enabled=false",
        "maintenance.cleanup.enabled=false",
        "kafka.consumer.auto-startup=false",
        "jwt.service.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "jwt.service.issuer=security-service",
        "jwt.service.audience=bank-service"
})
@Transactional
class BankServiceApplicationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JwtService jwtService;
    @Autowired ImportUserUseCase importUserUseCase;

    private String adminToken;

    @BeforeEach
    void setUpToken() {
        adminToken = jwtService.generateToken("security-service", null, "SERVICE");
    }

    private MockHttpServletRequestBuilder authorized(MockHttpServletRequestBuilder builder) {
        return builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken);
    }

    @Test
    void happy_path_create_users_accounts_and_do_operations() throws Exception {
        UUID aliceId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        UUID bobId = createUser("bob", "Bob", 22, "MALE", "BROWN");

        UUID aliceAcc = createAccount(aliceId);
        UUID bobAcc = createAccount(bobId);

        mvc.perform(authorized(post("/accounts/{id}/deposit", aliceAcc)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100.00}")))
                .andExpect(status().isOk());

        mvc.perform(authorized(post("/users/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + aliceId + "\",\"friendId\":\"" + bobId + "\"}")))
                .andExpect(status().isCreated());

        mvc.perform(authorized(post("/accounts/{from}/transfer", aliceAcc)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"toAccountId\":\"" + bobAcc + "\",\"amount\":25.00}")))
                .andExpect(status().isOk());

        mvc.perform(authorized(post("/accounts/{id}/withdraw", bobAcc)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.00}")))
                .andExpect(status().isOk());

        BigDecimal aliceBalance = readBigDecimal(
                mvc.perform(authorized(get("/accounts/{id}/balance", aliceAcc)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
        );

        BigDecimal bobBalance = readBigDecimal(
                mvc.perform(authorized(get("/accounts/{id}/balance", bobAcc)))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
        );

        assertThat(aliceBalance).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(bobBalance).isEqualByComparingTo(new BigDecimal("14.25"));

        String aliceDetailsJson = mvc.perform(authorized(get("/accounts/{id}", aliceAcc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.id").value(aliceAcc.toString()))
                .andReturn().getResponse().getContentAsString();

        JsonNode aliceDetails = objectMapper.readTree(aliceDetailsJson);
        assertThat(aliceDetails.get("transactions").isArray()).isTrue();
        assertThat(aliceDetails.get("transactions")).anySatisfy(node ->
                assertThat(node.get("type").asText()).isIn("DEPOSIT", "TRANSFER"));

        mvc.perform(authorized(get("/transactions").param("accountId", aliceAcc.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void withdraw_more_than_balance_returns_409_and_domain_error_code() throws Exception {
        UUID aliceId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        UUID aliceAcc = createAccount(aliceId);

        mvc.perform(authorized(post("/accounts/{id}/withdraw", aliceAcc)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10.00}")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("NOT_ENOUGH_MONEY"));
    }

    @Test
    void createUser_endpoint_disabled_returns_405() throws Exception {
        mvc.perform(authorized(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"login":"alice","name":"Alice","age":20,"sex":"FEMALE","hairColor":"BLONDE"}
                                """)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void getUsers_invalidSex_returns_400_invalid_argument() throws Exception {
        mvc.perform(authorized(get("/users")
                        .param("sex", "WRONG")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_ARGUMENT"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/users"));
    }

    @Test
    void createAccount_userNotFound_returns_404_not_found() throws Exception {
        mvc.perform(authorized(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerId":"00000000-0000-0000-0000-000000000000"}
                                """)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/accounts"));
    }

    @Test
    void deleteUser_with_accounts_and_friendships_cascades_and_returns_204() throws Exception {
        UUID aliceId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        UUID bobId = createUser("bob", "Bob", 22, "MALE", "BROWN");
        UUID aliceAcc = createAccount(aliceId);
        createAccount(bobId);

        mvc.perform(authorized(post("/users/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"" + aliceId + "\",\"friendId\":\"" + bobId + "\"}")))
                .andExpect(status().isCreated());

        mvc.perform(authorized(delete("/users/{userId}", aliceId)))
                .andExpect(status().isNoContent());

        mvc.perform(authorized(get("/users/{userId}", aliceId)))
                .andExpect(status().isNotFound());

        mvc.perform(authorized(get("/accounts/{accountId}", aliceAcc)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deposit_negativeAmount_returns_400_validation_error() throws Exception {
        UUID userId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        UUID accountId = createAccount(userId);

        mvc.perform(authorized(post("/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":-10}
                                """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/accounts/" + accountId + "/deposit"));

        assertThat(userId).isNotNull();
    }

    @Test
    void withdraw_negativeAmount_returns_400_validation_error() throws Exception {
        UUID userId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        UUID accountId = createAccount(userId);

        mvc.perform(authorized(post("/accounts/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":-1}
                                """)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/accounts/" + accountId + "/withdraw"));
    }

    @Test
    void transfer_toAccountNotFound_returns_404_not_found() throws Exception {
        UUID aliceId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        createUser("bob", "Bob", 22, "MALE", "BROWN");
        UUID fromAccountId = createAccount(aliceId);

        UUID randomTo = UUID.randomUUID();

        mvc.perform(authorized(post("/accounts/" + fromAccountId + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toAccountId":"%s","amount":10}
                                """.formatted(randomTo))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/accounts/" + fromAccountId + "/transfer"));
    }

    @Test
    void withdraw_moreThanBalance_returns_409_not_enough_money() throws Exception {
        UUID aliceId = createUser("alice", "Alice", 20, "FEMALE", "BLONDE");
        UUID accountId = createAccount(aliceId);

        mvc.perform(authorized(post("/accounts/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":10}
                                """)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("NOT_ENOUGH_MONEY"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/accounts/" + accountId + "/withdraw"));
    }

    private UUID createUser(String login, String name, int age, String sex, String hairColor) {
        UUID userId = UUID.randomUUID();
        ImportUserCommand command = new ImportUserCommand(
                userId,
                login,
                name,
                age,
                Sex.valueOf(sex),
                HairColor.valueOf(hairColor)
        );
        importUserUseCase.importUser(command);
        return userId;
    }

    private UUID createAccount(UUID ownerId) throws Exception {
        String response = mvc.perform(authorized(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"" + ownerId + "\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(response);
        return UUID.fromString(node.get("id").asText());
    }

    private BigDecimal readBigDecimal(String jsonNumber) {
        return new BigDecimal(jsonNumber.trim());
    }
}
