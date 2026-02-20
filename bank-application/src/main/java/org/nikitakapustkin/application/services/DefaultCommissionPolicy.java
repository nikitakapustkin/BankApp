package org.nikitakapustkin.application.services;

import java.math.BigDecimal;
import java.util.UUID;
import org.nikitakapustkin.domain.services.CommissionPolicy;

public class DefaultCommissionPolicy implements CommissionPolicy {
  private final BigDecimal friendsRate;
  private final BigDecimal othersRate;

  public DefaultCommissionPolicy(BigDecimal friendsRate, BigDecimal othersRate) {
    this.friendsRate = normalize(friendsRate);
    this.othersRate = normalize(othersRate);
  }

  @Override
  public BigDecimal rateFor(UUID fromUserId, UUID toUserId, boolean areFriends) {
    if (fromUserId == null || toUserId == null || fromUserId.equals(toUserId)) {
      return BigDecimal.ZERO;
    }
    return areFriends ? friendsRate : othersRate;
  }

  private static BigDecimal normalize(BigDecimal rate) {
    if (rate == null) {
      return BigDecimal.ZERO;
    }
    if (rate.signum() < 0) {
      throw new IllegalArgumentException("Commission rate must be non-negative");
    }
    return rate;
  }
}
