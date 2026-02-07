package org.nikitakapustkin.domain.events.payload;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransferredEventData(
        UUID accountId,
        UUID counterpartyAccountId,
        BigDecimal amount,
        TransferDirection direction,
        String description
) implements DomainEventData {}
