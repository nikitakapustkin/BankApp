package org.nikitakapustkin.security.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Value
public class AccountResponseDto {
    private UUID accountId;

    private UUID ownerId;

    private BigDecimal balance;

    private List<TransactionResponseDto> transactions;
}
