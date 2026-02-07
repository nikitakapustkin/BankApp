package org.nikitakapustkin.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.payload.DomainEventData;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DomainEvent {
    private UUID eventId;
    private UUID correlationId;
    private UUID entityId;
    private EventType eventType;
    private Instant eventTime;
    private String eventDescription;
    private DomainEventData payload;

    public static DomainEvent now(UUID entityId, EventType eventType, String eventDescription, UUID correlationId) {
        return new DomainEvent(
                UUID.randomUUID(),
                correlationId,
                entityId,
                eventType,
                Instant.now(),
                eventDescription,
                null
        );
    }

    public static DomainEvent now(UUID entityId,
                                  EventType eventType,
                                  String eventDescription,
                                  UUID correlationId,
                                  DomainEventData payload) {
        return new DomainEvent(
                UUID.randomUUID(),
                correlationId,
                entityId,
                eventType,
                Instant.now(),
                eventDescription,
                payload
        );
    }
}
