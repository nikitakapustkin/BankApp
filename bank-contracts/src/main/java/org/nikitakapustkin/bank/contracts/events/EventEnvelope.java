package org.nikitakapustkin.bank.contracts.events;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
    UUID eventId,
    String eventType,
    Instant occurredAt,
    UUID correlationId,
    String producer,
    T payload) {}
