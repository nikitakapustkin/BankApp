package org.nikitakapustkin.bank.contracts.events;

import java.util.UUID;

public record UnknownEventPayload(
        UUID entityId,
        String description
) {}
