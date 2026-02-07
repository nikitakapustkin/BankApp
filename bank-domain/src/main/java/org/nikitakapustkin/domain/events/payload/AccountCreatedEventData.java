package org.nikitakapustkin.domain.events.payload;

import java.util.UUID;

public record AccountCreatedEventData(
        UUID accountId,
        UUID ownerId,
        String ownerLogin,
        String description
) implements DomainEventData {}
