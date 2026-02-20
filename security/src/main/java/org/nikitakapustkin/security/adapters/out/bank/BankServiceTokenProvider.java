package org.nikitakapustkin.security.adapters.out.bank;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.adapters.out.jwt.ServiceJwtService;
import org.nikitakapustkin.security.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankServiceTokenProvider {
  private final ServiceJwtService jwtService;

  @Value("${jwt.service.subject:security-service}")
  private String subject;

  public String getToken() {
    return jwtService.generateToken(subject, Role.SERVICE);
  }
}
