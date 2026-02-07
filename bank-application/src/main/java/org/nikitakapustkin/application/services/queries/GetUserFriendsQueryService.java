package org.nikitakapustkin.application.services.queries;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.queries.GetUserFriendsQuery;
import org.nikitakapustkin.application.ports.out.LoadFriendsPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;
import org.nikitakapustkin.domain.models.User;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetUserFriendsQueryService implements GetUserFriendsQuery {

    private final LoadUserPort loadUserPort;
    private final LoadFriendsPort loadFriendsPort;

    @Override
    public List<User> getFriends(UUID userId) {
        User user = loadUserPort.loadUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        List<UUID> friendIds = loadFriendsPort.loadFriendsIds(user.getId());

        return friendIds.stream()
                .map(loadUserPort::loadUserById)
                .map(opt -> opt.orElseThrow(() -> new UserNotFoundException("Friend not found")))
                .toList();
    }
}
