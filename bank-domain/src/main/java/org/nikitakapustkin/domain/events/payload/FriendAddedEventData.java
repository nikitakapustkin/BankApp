package org.nikitakapustkin.domain.events.payload;

import java.util.UUID;

public record FriendAddedEventData(
        UUID userId,
        UUID friendId,
        String description
) implements DomainEventData {}
