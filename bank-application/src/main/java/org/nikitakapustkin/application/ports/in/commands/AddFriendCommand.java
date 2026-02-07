package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.SelfValidating;

import java.util.UUID;

@Getter
public class AddFriendCommand extends SelfValidating<AddFriendCommand> {

    @NotNull
    private final UUID userId;

    @NotNull
    private final UUID friendId;

    public AddFriendCommand(UUID userId, UUID friendId) {
        this.userId = userId;
        this.friendId = friendId;
        validateSelf();
    }
}
