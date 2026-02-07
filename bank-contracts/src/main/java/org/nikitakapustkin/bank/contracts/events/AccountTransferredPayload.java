package org.nikitakapustkin.bank.contracts.events;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransferredPayload(
        UUID accountId,
        UUID counterpartyAccountId,
        BigDecimal amount,
        TransferDirection direction,
        String description
) {}
