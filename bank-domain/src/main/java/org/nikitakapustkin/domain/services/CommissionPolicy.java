package org.nikitakapustkin.domain.services;

import java.math.BigDecimal;
import java.util.UUID;

public interface CommissionPolicy {
  BigDecimal rateFor(UUID fromUserId, UUID toUserId, boolean areFriends);
}
