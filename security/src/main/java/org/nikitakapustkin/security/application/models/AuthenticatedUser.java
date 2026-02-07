package org.nikitakapustkin.security.application.models;

import org.nikitakapustkin.security.enums.Role;

import java.util.UUID;

public record AuthenticatedUser(String username, UUID userId, Role role) {
}
