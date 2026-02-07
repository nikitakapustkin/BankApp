package org.nikitakapustkin.adapters.in.web.security;

import java.security.Principal;
import java.util.UUID;

public record JwtPrincipal(String login, UUID userId, String role) implements Principal {

    @Override
    public String getName() {
        return login;
    }
}
