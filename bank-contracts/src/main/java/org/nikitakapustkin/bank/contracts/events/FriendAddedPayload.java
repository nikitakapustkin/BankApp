package org.nikitakapustkin.bank.contracts.events;

import java.util.UUID;

public record FriendAddedPayload(
        UUID userId,
        UUID friendId,
        String description
) {}
