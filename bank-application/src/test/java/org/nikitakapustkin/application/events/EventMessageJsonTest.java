package org.nikitakapustkin.application.events;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;

class EventMessageJsonTest {

  @Test
  void event_message_serializes_with_expected_fields() throws Exception {
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    UUID eventId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();
    UUID entityId = UUID.randomUUID();
    Instant eventTime = Instant.parse("2024-01-01T00:00:00Z");

    DomainEvent event =
        new DomainEvent(
            eventId,
            correlationId,
            entityId,
            EventType.USER_CREATED,
            eventTime,
            "User created",
            null);

    String json = mapper.writeValueAsString(event);
    assertThat(json).contains("\"entityId\"");

    DomainEvent roundTrip = mapper.readValue(json, DomainEvent.class);
    assertThat(roundTrip.getEventId()).isEqualTo(eventId);
    assertThat(roundTrip.getCorrelationId()).isEqualTo(correlationId);
    assertThat(roundTrip.getEntityId()).isEqualTo(entityId);
    assertThat(roundTrip.getEventType()).isEqualTo(EventType.USER_CREATED);
    assertThat(roundTrip.getEventTime()).isEqualTo(eventTime);
    assertThat(roundTrip.getEventDescription()).isEqualTo("User created");
    assertThat(roundTrip.getPayload()).isNull();
  }
}
