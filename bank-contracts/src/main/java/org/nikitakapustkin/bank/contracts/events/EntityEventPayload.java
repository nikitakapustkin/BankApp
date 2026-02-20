package org.nikitakapustkin.bank.contracts.events;

import java.util.UUID;

public record EntityEventPayload(UUID entityId, String description) {}
