package org.nikitakapustkin.bank.contracts.events;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountWithdrawnPayload(UUID accountId, BigDecimal amount, String description) {}
