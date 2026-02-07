package org.nikitakapustkin.security.application.ports.out;

import org.nikitakapustkin.security.enums.Role;

import java.util.UUID;

public interface JwtIssuerPort {
    String generateToken(String login, UUID userId, Role role);
}
