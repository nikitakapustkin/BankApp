package org.nikitakapustkin.bank.contracts.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record DepositRequestDto(
    @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive")
        BigDecimal amount) {}
