package org.nikitakapustkin.domain.events.payload;

import java.util.UUID;

public record FriendRemovedEventData(UUID userId, UUID friendId, String description)
    implements DomainEventData {}
