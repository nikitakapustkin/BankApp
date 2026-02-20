package org.nikitakapustkin.security.application.models;

import java.util.UUID;
import org.nikitakapustkin.security.enums.Role;

public record AuthenticatedUser(String username, UUID userId, Role role) {}
