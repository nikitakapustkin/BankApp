package org.nikitakapustkin.security.application.ports.out;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.security.dto.UserResponseDto;

public interface UserBankClientPort {
  List<UserResponseDto> getUsers(String hairColor, String sex);

  UserResponseDto getUserInfo(UUID userId);

  void createFriendship(UUID ownerId, UUID friendId);

  void removeFriendship(UUID ownerId, UUID friendId);

  void deleteUser(UUID userId);
}
