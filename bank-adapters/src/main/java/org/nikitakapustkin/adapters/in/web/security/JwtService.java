package org.nikitakapustkin.adapters.in.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private static final String CLAIM_USER_ID = "userId";
  private static final String CLAIM_ROLE = "role";

  @Value("${jwt.service.secret}")
  private String secret;

  @Value("${jwt.service.issuer}")
  private String issuer;

  @Value("${jwt.service.audience}")
  private String audience;

  @Value("${jwt.service.allowed-subjects:security-service}")
  private String allowedSubjects;

  private Set<String> allowedSubjectSet;

  @PostConstruct
  void validateSecret() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException(
          "jwt.service.secret is not set. Provide it via env var JWT_SERVICE_SECRET or application properties.");
    }
    if (issuer == null || issuer.isBlank()) {
      throw new IllegalStateException("jwt.service.issuer is not set.");
    }
    if (audience == null || audience.isBlank()) {
      throw new IllegalStateException("jwt.service.audience is not set.");
    }
    if (allowedSubjects == null || allowedSubjects.isBlank()) {
      throw new IllegalStateException("jwt.service.allowed-subjects is not set.");
    }
    allowedSubjectSet =
        Arrays.stream(allowedSubjects.split(","))
            .map(String::trim)
            .filter(subject -> !subject.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    if (allowedSubjectSet.isEmpty()) {
      throw new IllegalStateException(
          "jwt.service.allowed-subjects must contain at least one value.");
    }
  }

  public String generateToken(String login, UUID userId, String role) {
    Map<String, Object> claims = new HashMap<>();
    if (userId != null) {
      claims.put(CLAIM_USER_ID, userId.toString());
    }
    if (role != null && !role.isBlank()) {
      claims.put(CLAIM_ROLE, role);
    }
    return Jwts.builder()
        .claims()
        .add(claims)
        .issuer(issuer)
        .audience()
        .add(audience)
        .and()
        .subject(login)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
        .and()
        .signWith(getKey())
        .compact();
  }

  private SecretKey getKey() {
    byte[] encodedKey = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(encodedKey);
  }

  public String extractUsername(String token) {
    return extractClaims(token, Claims::getSubject);
  }

  public UUID extractUserId(String token) {
    String raw = extractClaims(token, claims -> claims.get(CLAIM_USER_ID, String.class));
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(raw);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public String extractRole(String token) {
    return extractClaims(token, claims -> claims.get(CLAIM_ROLE, String.class));
  }

  public boolean isAllowedSubject(String subject) {
    return subject != null && allowedSubjectSet.contains(subject);
  }

  private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
    Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getKey())
        .requireIssuer(issuer)
        .requireAudience(audience)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean validateToken(String token) {
    try {
      return !isTokenExpired(token);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaims(token, Claims::getExpiration);
  }
}
