package org.nikitakapustkin.security.application.ports.out;

import org.nikitakapustkin.security.models.User;

public interface UserRepositoryPort {
    User findUserByLogin(String login);

    boolean existsByLogin(String login);

    User save(User user);
}
