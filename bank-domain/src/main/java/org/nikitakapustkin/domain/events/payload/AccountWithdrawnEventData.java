package org.nikitakapustkin.domain.events.payload;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountWithdrawnEventData(
        UUID accountId,
        BigDecimal amount,
        String description
) implements DomainEventData {}
