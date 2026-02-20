package org.nikitakapustkin.bank.contracts.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;

public record TransactionCreatedPayload(
    UUID transactionId,
    UUID accountId,
    TransactionType transactionType,
    BigDecimal amount,
    Instant createdAt) {}
