package org.nikitakapustkin.security.adapters.out.persistence;

import org.nikitakapustkin.security.application.ports.out.UserRepositoryPort;
import org.nikitakapustkin.security.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UserRepositoryPort {
    User findUserByLogin(String login);

    boolean existsByLogin(String login);
}
