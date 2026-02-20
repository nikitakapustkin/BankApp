package org.nikitakapustkin.security.application.ports.out;

public interface PasswordHasherPort {
  String encode(String rawPassword);
}
