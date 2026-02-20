package org.nikitakapustkin.application.ports.out;

import java.util.Optional;
import java.util.UUID;
import org.nikitakapustkin.domain.models.User;

public interface LoadUserPort {
  Optional<User> loadUserByLogin(String login);

  Optional<User> loadUserById(UUID id);
}
