package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.SelfValidating;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class DepositMoneyCommand extends SelfValidating<DepositMoneyCommand> {

    @NotNull
    private final UUID accountId;

    @NotNull
    @DecimalMin(value = "0.01")
    private final BigDecimal amount;

    public DepositMoneyCommand(UUID accountId, BigDecimal amount) {
        this.accountId = accountId;
        this.amount = amount;
        validateSelf();
    }
}
