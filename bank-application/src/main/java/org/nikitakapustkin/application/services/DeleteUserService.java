package org.nikitakapustkin.application.services;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.DeleteUserUseCase;
import org.nikitakapustkin.application.ports.out.DeleteUserPort;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;

@RequiredArgsConstructor
public class DeleteUserService implements DeleteUserUseCase {

  private final DeleteUserPort deleteUserPort;

  @Override
  public void deleteUser(UUID userId) {
    boolean deleted = deleteUserPort.deleteById(userId);
    if (!deleted) {
      throw new UserNotFoundException("User not found: " + userId);
    }
  }
}
