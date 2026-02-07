package org.nikitakapustkin.bank.contracts.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequestDto(
        @NotNull(message = "To account ID is required")
        UUID toAccountId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {}
