package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.SelfValidating;

@Getter
public class WithdrawMoneyCommand extends SelfValidating<WithdrawMoneyCommand> {

  @NotNull private final UUID accountId;

  @NotNull
  @DecimalMin(value = "0.01")
  private final BigDecimal amount;

  public WithdrawMoneyCommand(UUID accountId, BigDecimal amount) {
    this.accountId = accountId;
    this.amount = amount;
    validateSelf();
  }
}
