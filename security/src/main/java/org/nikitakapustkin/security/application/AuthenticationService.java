package org.nikitakapustkin.security.application;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.models.AuthenticatedUser;
import org.nikitakapustkin.security.application.ports.out.CredentialsAuthenticatorPort;
import org.nikitakapustkin.security.application.ports.out.JwtIssuerPort;
import org.nikitakapustkin.security.dto.LoginRequestDto;
import org.nikitakapustkin.security.exceptions.AuthenticationFailedException;

@RequiredArgsConstructor
public class AuthenticationService {
  private final CredentialsAuthenticatorPort authenticator;
  private final JwtIssuerPort jwtIssuer;

  public String authenticate(LoginRequestDto loginRequest) {
    AuthenticatedUser user =
        authenticator.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
    if (user == null) {
      throw new AuthenticationFailedException("Invalid username or password");
    }
    return jwtIssuer.generateToken(user.username(), user.userId(), user.role());
  }
}
