package org.nikitakapustkin.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nikitakapustkin.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private UUID id;
    private UUID accountId;

    private TransactionType transactionType;
    private BigDecimal amount;

    private Instant createdAt;
}
