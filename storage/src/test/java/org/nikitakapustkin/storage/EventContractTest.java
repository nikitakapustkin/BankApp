package org.nikitakapustkin.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.bank.contracts.events.AccountCreatedPayload;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.TransactionCreatedPayload;

class EventContractTest {

  @Test
  void producer_event_message_is_compatible_with_event_envelope() throws Exception {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    UUID eventId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    UUID entityId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<AccountCreatedPayload> message =
        new EventEnvelope<>(
            eventId,
            EventTypes.ACCOUNT_CREATED,
            eventTime,
            correlationId,
            "bank-service",
            new AccountCreatedPayload(entityId, ownerId, "alice", "Account created"));

    String json = mapper.writeValueAsString(message);
    EventEnvelope<AccountCreatedPayload> dto = mapper.readValue(json, new TypeReference<>() {});

    assertThat(dto.eventId()).isEqualTo(eventId);
    assertThat(dto.correlationId()).isEqualTo(correlationId);
    assertThat(dto.payload().accountId()).isEqualTo(entityId);
    assertThat(dto.payload().ownerId()).isEqualTo(ownerId);
    assertThat(dto.eventType()).isEqualTo(EventTypes.ACCOUNT_CREATED);
    assertThat(dto.occurredAt()).isEqualTo(eventTime);
    assertThat(dto.payload().description()).isEqualTo("Account created");
  }

  @Test
  void producer_transaction_message_is_compatible_with_event_envelope() throws Exception {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    UUID transactionId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    Instant createdAt = Instant.parse("2024-01-01T00:00:00Z");

    EventEnvelope<TransactionCreatedPayload> message =
        new EventEnvelope<>(
            transactionId,
            EventTypes.TRANSACTION_CREATED,
            createdAt,
            correlationId,
            "bank-service",
            new TransactionCreatedPayload(
                transactionId,
                accountId,
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                createdAt));

    String json = mapper.writeValueAsString(message);
    EventEnvelope<TransactionCreatedPayload> dto = mapper.readValue(json, new TypeReference<>() {});

    assertThat(dto.eventId()).isEqualTo(transactionId);
    assertThat(dto.correlationId()).isEqualTo(correlationId);
    assertThat(dto.payload().accountId()).isEqualTo(accountId);
    assertThat(dto.payload().transactionType()).isEqualTo(TransactionType.TRANSFER);
    assertThat(dto.payload().amount()).isEqualTo(new BigDecimal("100.00"));
    assertThat(dto.payload().createdAt()).isEqualTo(createdAt);
  }
}
