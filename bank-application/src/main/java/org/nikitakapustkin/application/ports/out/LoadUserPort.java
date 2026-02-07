package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.User;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<User> loadUserByLogin(String login);
    Optional<User> loadUserById(UUID id);
}
