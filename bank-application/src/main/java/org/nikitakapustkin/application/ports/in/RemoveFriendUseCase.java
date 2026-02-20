package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.RemoveFriendCommand;

public interface RemoveFriendUseCase {
  void removeFriend(RemoveFriendCommand command);
}
