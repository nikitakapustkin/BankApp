package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.SelfValidating;

import java.util.UUID;

@Getter
public class RemoveFriendCommand extends SelfValidating<RemoveFriendCommand> {

    @NotNull
    private final UUID userId;

    @NotNull
    private final UUID friendId;

    public RemoveFriendCommand(UUID userId, UUID friendId) {
        this.userId = userId;
        this.friendId = friendId;
        validateSelf();
    }
}
