package org.nikitakapustkin.application.ports.out;

import java.util.UUID;

public interface UpdateFriendsPort {
  void addFriend(UUID userId, UUID friendId);

  void removeFriend(UUID userId, UUID friendId);
}
