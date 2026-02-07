package org.nikitakapustkin.security.dto;

import lombok.Value;

import org.nikitakapustkin.bank.contracts.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
public class TransactionResponseDto {
    private UUID transactionId;
    private UUID accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private Instant createdAt;
}
