package org.nikitakapustkin.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.bank.contracts.events.AccountDepositedPayload;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.TransactionCreatedPayload;
import org.nikitakapustkin.bank.contracts.events.UserCreatedPayload;
import org.nikitakapustkin.storage.application.EventIngestionService;
import org.nikitakapustkin.storage.application.ports.out.AccountEventRepositoryPort;
import org.nikitakapustkin.storage.application.ports.out.TransactionEventRepositoryPort;
import org.nikitakapustkin.storage.application.ports.out.UserEventRepositoryPort;
import org.nikitakapustkin.storage.events.AccountEvent;
import org.nikitakapustkin.storage.events.TransactionEvent;
import org.nikitakapustkin.storage.events.UserEvent;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

  @Mock private UserEventRepositoryPort userEventRepository;
  @Mock private AccountEventRepositoryPort accountEventRepository;
  @Mock private TransactionEventRepositoryPort transactionEventRepository;

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void consume_user_events_saves_user_event() throws Exception {
    UUID eventId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<UserCreatedPayload> envelope =
        new EventEnvelope<>(
            eventId,
            EventTypes.USER_CREATED,
            eventTime,
            correlationId,
            "bank-service",
            new UserCreatedPayload(
                userId, "alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK, "User created"));
    String payload = objectMapper.writeValueAsString(envelope);

    EventIngestionService service =
        new EventIngestionService(
            userEventRepository, accountEventRepository, transactionEventRepository, objectMapper);

    service.consumeUserEvents(payload);

    ArgumentCaptor<UserEvent> captor = ArgumentCaptor.forClass(UserEvent.class);
    verify(userEventRepository).save(captor.capture());

    UserEvent saved = captor.getValue();
    assertThat(saved.getEventId()).isEqualTo(eventId);
    assertThat(saved.getCorrelationId()).isEqualTo(correlationId);
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getEventType()).isEqualTo(EventTypes.USER_CREATED);
    assertThat(saved.getEventTime()).isEqualTo(eventTime);
    assertThat(saved.getEventDescription()).isEqualTo("User created");
    assertThat(saved.getPayloadType()).isEqualTo(UserCreatedPayload.class.getName());
    UserCreatedPayload actualPayload =
        objectMapper.readValue(saved.getPayload(), UserCreatedPayload.class);
    assertThat(actualPayload).isEqualTo(envelope.payload());
  }

  @Test
  void consume_account_events_falls_back_to_event_id_when_correlation_missing() throws Exception {
    UUID eventId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<AccountDepositedPayload> envelope =
        new EventEnvelope<>(
            eventId,
            EventTypes.ACCOUNT_DEPOSIT,
            eventTime,
            null,
            "bank-service",
            new AccountDepositedPayload(accountId, new BigDecimal("10.00"), "Deposit"));
    String payload = objectMapper.writeValueAsString(envelope);

    EventIngestionService service =
        new EventIngestionService(
            userEventRepository, accountEventRepository, transactionEventRepository, objectMapper);

    service.consumeAccountEvents(payload);

    ArgumentCaptor<AccountEvent> captor = ArgumentCaptor.forClass(AccountEvent.class);
    verify(accountEventRepository).save(captor.capture());

    AccountEvent saved = captor.getValue();
    assertThat(saved.getEventId()).isEqualTo(eventId);
    assertThat(saved.getCorrelationId()).isEqualTo(eventId);
    assertThat(saved.getAccountId()).isEqualTo(accountId);
    assertThat(saved.getEventType()).isEqualTo(EventTypes.ACCOUNT_DEPOSIT);
    assertThat(saved.getEventTime()).isEqualTo(eventTime);
    assertThat(saved.getEventDescription()).isEqualTo("Deposit");
    assertThat(saved.getPayloadType()).isEqualTo(AccountDepositedPayload.class.getName());
    AccountDepositedPayload actualPayload =
        objectMapper.readValue(saved.getPayload(), AccountDepositedPayload.class);
    assertThat(actualPayload.accountId()).isEqualTo(accountId);
    assertThat(actualPayload.amount()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(actualPayload.description()).isEqualTo("Deposit");
  }

  @Test
  void consume_transaction_events_saves_transaction_event() throws Exception {
    UUID transactionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<TransactionCreatedPayload> envelope =
        new EventEnvelope<>(
            transactionId,
            EventTypes.TRANSACTION_CREATED,
            createdAt,
            correlationId,
            "bank-service",
            new TransactionCreatedPayload(
                transactionId,
                accountId,
                TransactionType.DEPOSIT,
                new BigDecimal("10.50"),
                createdAt));
    String payload = objectMapper.writeValueAsString(envelope);

    EventIngestionService service =
        new EventIngestionService(
            userEventRepository, accountEventRepository, transactionEventRepository, objectMapper);

    service.consumeTransactionEvents(payload);

    ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
    verify(transactionEventRepository).save(captor.capture());

    TransactionEvent saved = captor.getValue();
    assertThat(saved.getEventId()).isEqualTo(transactionId);
    assertThat(saved.getTransactionId()).isEqualTo(transactionId);
    assertThat(saved.getCorrelationId()).isEqualTo(correlationId);
    assertThat(saved.getAccountId()).isEqualTo(accountId);
    assertThat(saved.getEventType()).isEqualTo(EventTypes.TRANSACTION_CREATED);
    assertThat(saved.getEventTime()).isEqualTo(createdAt);
    assertThat(saved.getEventDescription()).isNull();
    assertThat(saved.getPayloadType()).isEqualTo(TransactionCreatedPayload.class.getName());
    assertThat(saved.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
    assertThat(saved.getAmount()).isEqualTo(new BigDecimal("10.50"));
    assertThat(saved.getCreatedAt()).isEqualTo(createdAt);
    TransactionCreatedPayload actualPayload =
        objectMapper.readValue(saved.getPayload(), TransactionCreatedPayload.class);
    assertThat(actualPayload.transactionId()).isEqualTo(transactionId);
    assertThat(actualPayload.accountId()).isEqualTo(accountId);
    assertThat(actualPayload.transactionType()).isEqualTo(TransactionType.DEPOSIT);
    assertThat(actualPayload.amount()).isEqualByComparingTo(new BigDecimal("10.50"));
    assertThat(actualPayload.createdAt()).isEqualTo(createdAt);
  }
}
