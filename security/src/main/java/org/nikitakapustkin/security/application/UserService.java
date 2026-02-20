package org.nikitakapustkin.security.application;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.ports.out.PasswordHasherPort;
import org.nikitakapustkin.security.application.ports.out.UserBankClientPort;
import org.nikitakapustkin.security.application.ports.out.UserEventPublisherPort;
import org.nikitakapustkin.security.application.ports.out.UserRepositoryPort;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;
import org.nikitakapustkin.security.dto.UserResponseDto;
import org.nikitakapustkin.security.enums.Role;
import org.nikitakapustkin.security.exceptions.UserAlreadyExistsException;
import org.nikitakapustkin.security.models.User;

@RequiredArgsConstructor
public class UserService {
  private final UserRepositoryPort userRepository;
  private final UserBankClientPort userBankClient;
  private final PasswordHasherPort passwordHasher;
  private final UserEventPublisherPort userEventPublisher;

  public void createUser(UserCreateRequestDto userCreateDto) {
    if (userRepository.existsByLogin(userCreateDto.getLogin())) {
      throw new UserAlreadyExistsException(
          "User with login: " + userCreateDto.getLogin() + " already exists");
    }
    if (userCreateDto.getAge() <= 0) {
      throw new IllegalArgumentException("Age must be positive");
    }

    UUID id = UUID.randomUUID();
    User user =
        new User(
            id,
            userCreateDto.getLogin(),
            passwordHasher.encode(userCreateDto.getPassword()),
            Role.CLIENT);
    userRepository.save(user);
    userEventPublisher.enqueueUserCreated(userCreateDto, id);
  }

  public List<UserResponseDto> getUsers(String hairColor, String sex) {
    return userBankClient.getUsers(hairColor, sex);
  }

  public UserResponseDto getUserInfo(UUID userId) {
    return userBankClient.getUserInfo(userId);
  }

  public void createFriendship(UUID ownerId, UUID friendId) {
    userBankClient.createFriendship(ownerId, friendId);
  }

  public void deleteFriendship(UUID ownerId, UUID friendId) {
    userBankClient.removeFriendship(ownerId, friendId);
  }
}
