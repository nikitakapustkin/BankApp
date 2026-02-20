package org.nikitakapustkin.security.application.ports.out;

import java.util.UUID;
import org.nikitakapustkin.security.enums.Role;

public interface JwtIssuerPort {
  String generateToken(String login, UUID userId, Role role);
}
