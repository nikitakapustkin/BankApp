package org.nikitakapustkin.application.ports.out;

import java.util.List;
import java.util.UUID;

public interface LoadFriendsPort {
    List<UUID> loadFriendsIds(UUID userId);
}
