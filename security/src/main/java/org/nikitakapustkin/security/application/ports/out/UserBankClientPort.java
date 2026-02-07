package org.nikitakapustkin.security.application.ports.out;

import org.nikitakapustkin.security.dto.UserResponseDto;

import java.util.List;
import java.util.UUID;

public interface UserBankClientPort {
    List<UserResponseDto> getUsers(String hairColor, String sex);

    UserResponseDto getUserInfo(UUID userId);

    void createFriendship(UUID ownerId, UUID friendId);

    void removeFriendship(UUID ownerId, UUID friendId);

    void deleteUser(UUID userId);
}
