package org.nikitakapustkin.application.ports.in.queries;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.domain.models.User;

public interface GetUserFriendsQuery {
  List<User> getFriends(UUID userId);
}
