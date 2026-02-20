package org.nikitakapustkin.bank.contracts.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;

public record TransactionResponseDto(
    UUID id, UUID accountId, TransactionType type, BigDecimal amount, Instant createdAt) {}
