package org.nikitakapustkin.bank.contracts.dto.response;

import org.nikitakapustkin.bank.contracts.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponseDto(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        Instant createdAt
) {}
