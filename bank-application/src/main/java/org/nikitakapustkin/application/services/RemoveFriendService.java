package org.nikitakapustkin.application.services;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.RemoveFriendUseCase;
import org.nikitakapustkin.application.ports.in.commands.RemoveFriendCommand;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.application.ports.out.UpdateFriendsPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.FriendRemovedEventData;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;

@RequiredArgsConstructor
public class RemoveFriendService implements RemoveFriendUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateFriendsPort updateFriendsPort;
    private final PublishUserEventPort publishUserEventPort;

    @Override
    public void removeFriend(RemoveFriendCommand cmd) {
        var user = loadUserPort.loadUserById(cmd.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + cmd.getUserId()));
        var friend = loadUserPort.loadUserById(cmd.getFriendId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + cmd.getFriendId()));

        updateFriendsPort.removeFriend(cmd.getUserId(), cmd.getFriendId());

        String description = "Friend removed: " + cmd.getUserId() + " -> " + cmd.getFriendId();
        publishUserEventPort.publishUserEvent(DomainEvent.now(
                user.getId(),
                EventType.FRIEND_REMOVED,
                description,
                friend.getId(),
                new FriendRemovedEventData(
                        user.getId(),
                        friend.getId(),
                        description
                )
        ));
    }
}
