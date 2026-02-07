package org.nikitakapustkin.security.application.ports.out;

import org.nikitakapustkin.security.application.models.AuthenticatedUser;

public interface CredentialsAuthenticatorPort {
    AuthenticatedUser authenticate(String username, String password);
}
