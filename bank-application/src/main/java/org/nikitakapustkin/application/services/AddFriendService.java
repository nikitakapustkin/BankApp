package org.nikitakapustkin.application.services;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.AddFriendUseCase;
import org.nikitakapustkin.application.ports.in.commands.AddFriendCommand;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.application.ports.out.UpdateFriendsPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.FriendAddedEventData;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;

@RequiredArgsConstructor
public class AddFriendService implements AddFriendUseCase {

    private final LoadUserPort loadUserPort;
    private final UpdateFriendsPort updateFriendsPort;
    private final PublishUserEventPort publishUserEventPort;

    @Override
    public void addFriend(AddFriendCommand cmd) {
        var user = loadUserPort.loadUserById(cmd.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + cmd.getUserId()));
        var friend = loadUserPort.loadUserById(cmd.getFriendId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + cmd.getFriendId()));

        updateFriendsPort.addFriend(cmd.getUserId(), cmd.getFriendId());

        String description = "Friend added: " + cmd.getUserId() + " -> " + cmd.getFriendId();
        publishUserEventPort.publishUserEvent(DomainEvent.now(
                user.getId(),
                EventType.FRIEND_ADDED,
                description,
                friend.getId(),
                new FriendAddedEventData(
                        user.getId(),
                        friend.getId(),
                        description
                )
        ));
    }
}
