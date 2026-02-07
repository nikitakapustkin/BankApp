package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.DifferentAccounts;
import org.nikitakapustkin.application.ports.in.SelfValidating;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@DifferentAccounts
public class TransferMoneyCommand extends SelfValidating<TransferMoneyCommand> {

        @NotNull
        private final UUID fromAccountId;

        @NotNull
        private final UUID toAccountId;

        @NotNull
        @DecimalMin(value = "0.01")
        private final BigDecimal amount;

        public TransferMoneyCommand(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
                this.fromAccountId = fromAccountId;
                this.toAccountId = toAccountId;
                this.amount = amount;
                validateSelf();
        }
}
