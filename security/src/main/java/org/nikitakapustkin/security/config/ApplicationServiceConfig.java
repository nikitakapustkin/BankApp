package org.nikitakapustkin.security.config;

import org.nikitakapustkin.security.application.AccountService;
import org.nikitakapustkin.security.application.AuthenticationService;
import org.nikitakapustkin.security.application.EventService;
import org.nikitakapustkin.security.application.UserService;
import org.nikitakapustkin.security.application.ports.out.AccountBankClientPort;
import org.nikitakapustkin.security.application.ports.out.CredentialsAuthenticatorPort;
import org.nikitakapustkin.security.application.ports.out.JwtIssuerPort;
import org.nikitakapustkin.security.application.ports.out.PasswordHasherPort;
import org.nikitakapustkin.security.application.ports.out.StorageEventClientPort;
import org.nikitakapustkin.security.application.ports.out.UserBankClientPort;
import org.nikitakapustkin.security.application.ports.out.UserEventPublisherPort;
import org.nikitakapustkin.security.application.ports.out.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServiceConfig {

  @Bean
  public AuthenticationService authenticationService(
      CredentialsAuthenticatorPort authenticator, JwtIssuerPort jwtIssuer) {
    return new AuthenticationService(authenticator, jwtIssuer);
  }

  @Bean
  public UserService userService(
      UserRepositoryPort userRepository,
      UserBankClientPort userBankClient,
      PasswordHasherPort passwordHasher,
      UserEventPublisherPort userEventPublisher) {
    return new UserService(userRepository, userBankClient, passwordHasher, userEventPublisher);
  }

  @Bean
  public AccountService accountService(AccountBankClientPort accountBankClient) {
    return new AccountService(accountBankClient);
  }

  @Bean
  public EventService eventService(StorageEventClientPort storageEventClient) {
    return new EventService(storageEventClient);
  }
}
