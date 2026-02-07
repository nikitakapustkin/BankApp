package org.nikitakapustkin.bank.contracts.events;

import java.util.UUID;

public record FriendRemovedPayload(
        UUID userId,
        UUID friendId,
        String description
) {}
