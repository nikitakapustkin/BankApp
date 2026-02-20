package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.User;

public interface CreateUserPort {
  User create(User user);
}
