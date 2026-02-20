package org.nikitakapustkin.security.adapters.out.security;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.models.AuthenticatedUser;
import org.nikitakapustkin.security.application.ports.out.CredentialsAuthenticatorPort;
import org.nikitakapustkin.security.models.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationManagerAdapter implements CredentialsAuthenticatorPort {
  private final AuthenticationManager authenticationManager;

  @Override
  public AuthenticatedUser authenticate(String username, String password) {
    Authentication authentication;
    try {
      authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(username, password));
    } catch (AuthenticationException ex) {
      return null;
    }

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserPrincipal userPrincipal) {
      return new AuthenticatedUser(
          userPrincipal.getUsername(), userPrincipal.getUserId(), userPrincipal.getRole());
    }
    return new AuthenticatedUser(username, null, null);
  }
}
