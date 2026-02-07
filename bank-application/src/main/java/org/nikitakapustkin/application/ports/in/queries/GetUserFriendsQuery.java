package org.nikitakapustkin.application.ports.in.queries;

import org.nikitakapustkin.domain.models.User;

import java.util.List;
import java.util.UUID;

public interface GetUserFriendsQuery {
    List<User> getFriends(UUID userId);
}
