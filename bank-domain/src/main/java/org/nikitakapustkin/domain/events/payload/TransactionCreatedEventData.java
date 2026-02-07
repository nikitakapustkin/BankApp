package org.nikitakapustkin.domain.events.payload;

import org.nikitakapustkin.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedEventData(
        UUID transactionId,
        UUID accountId,
        TransactionType transactionType,
        BigDecimal amount,
        Instant createdAt
) implements DomainEventData {}
