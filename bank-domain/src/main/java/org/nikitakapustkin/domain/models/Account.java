package org.nikitakapustkin.domain.models;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nikitakapustkin.domain.exceptions.NotEnoughMoneyException;
import org.nikitakapustkin.domain.services.MoneyRules;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
  private UUID id;
  private UUID userId;

  private String ownerLogin;
  private BigDecimal balance;

  public void deposit(BigDecimal amount) {
    MoneyRules.requirePositive(amount);
    if (balance == null) balance = BigDecimal.ZERO;
    balance = balance.add(amount);
  }

  public void withdraw(BigDecimal amount) {
    MoneyRules.requirePositive(amount);
    if (balance == null) balance = BigDecimal.ZERO;
    if (balance.compareTo(amount) < 0) {
      throw new NotEnoughMoneyException("Not enough money");
    }
    balance = balance.subtract(amount);
  }
}
