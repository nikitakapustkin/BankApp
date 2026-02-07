package org.nikitakapustkin.domain.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public final class TransferRules {
    private TransferRules() {
    }

    public static TransferCalculation calculate(UUID fromAccountId,
                                                UUID toAccountId,
                                                UUID fromUserId,
                                                UUID toUserId,
                                                BigDecimal amount,
                                                boolean areFriends,
                                                CommissionPolicy commissionPolicy) {
        if (fromAccountId == null || toAccountId == null) {
            throw new IllegalArgumentException("Account id must be provided");
        }
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("From and to accounts must be different");
        }
        MoneyRules.requirePositive(amount);

        BigDecimal rate = commissionPolicy.rateFor(fromUserId, toUserId, areFriends);
        if (rate == null) {
            rate = BigDecimal.ZERO;
        }
        if (rate.signum() < 0) {
            throw new IllegalArgumentException("Commission rate must be non-negative");
        }

        BigDecimal commission = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal credited = amount.subtract(commission);
        if (credited.signum() <= 0) {
            throw new IllegalArgumentException("Amount is too small after commission");
        }

        return new TransferCalculation(commission, credited);
    }

    public record TransferCalculation(BigDecimal commission, BigDecimal credited) {}
}
