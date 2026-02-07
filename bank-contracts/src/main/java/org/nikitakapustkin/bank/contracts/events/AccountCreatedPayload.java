package org.nikitakapustkin.bank.contracts.events;

import java.util.UUID;

public record AccountCreatedPayload(
        UUID accountId,
        UUID ownerId,
        String ownerLogin,
        String description
) {}
