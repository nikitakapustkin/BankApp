package org.nikitakapustkin.domain.services;

import java.math.BigDecimal;

public final class MoneyRules {
  private MoneyRules() {}

  public static void requirePositive(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
  }
}
