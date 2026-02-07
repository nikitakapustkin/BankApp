package org.nikitakapustkin.security.models;

import java.security.Principal;
import java.util.UUID;

public record JwtPrincipal(String login, UUID userId, String role) implements Principal {

    @Override
    public String getName() {
        return login;
    }
}
