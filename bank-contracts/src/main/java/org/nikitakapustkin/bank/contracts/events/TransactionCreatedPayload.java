package org.nikitakapustkin.bank.contracts.events;

import org.nikitakapustkin.bank.contracts.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionCreatedPayload(
        UUID transactionId,
        UUID accountId,
        TransactionType transactionType,
        BigDecimal amount,
        Instant createdAt
) {}
