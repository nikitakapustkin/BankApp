package org.nikitakapustkin.security.adapters.in.web.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistService {
  private static final long DEFAULT_TTL_SECONDS = 24 * 60 * 60;

  private final Map<String, Instant> revokedTokenExpirations = new ConcurrentHashMap<>();

  public void revoke(String token, Instant expiresAt) {
    if (token == null || token.isBlank()) {
      return;
    }
    Instant expiration =
        expiresAt != null ? expiresAt : Instant.now().plusSeconds(DEFAULT_TTL_SECONDS);
    revokedTokenExpirations.put(fingerprint(token), expiration);
    cleanupExpired();
  }

  public boolean isRevoked(String token) {
    if (token == null || token.isBlank()) {
      return false;
    }
    String key = fingerprint(token);
    Instant expiration = revokedTokenExpirations.get(key);
    if (expiration == null) {
      return false;
    }
    if (expiration.isBefore(Instant.now())) {
      revokedTokenExpirations.remove(key);
      return false;
    }
    return true;
  }

  private void cleanupExpired() {
    Instant now = Instant.now();
    revokedTokenExpirations.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
  }

  private static String fingerprint(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
