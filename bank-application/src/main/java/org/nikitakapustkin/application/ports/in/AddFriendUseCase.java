package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.AddFriendCommand;

public interface AddFriendUseCase {
  void addFriend(AddFriendCommand command);
}
