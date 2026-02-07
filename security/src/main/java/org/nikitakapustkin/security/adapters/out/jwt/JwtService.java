package org.nikitakapustkin.security.adapters.out.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.nikitakapustkin.security.application.ports.out.JwtIssuerPort;
import org.nikitakapustkin.security.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService implements JwtIssuerPort {
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    @Value("${jwt.secret}")
    private String secret;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret is not set. Provide it via env var JWT_SECRET (recommended) or application properties."
            );
        }
    }

    @Override
    public String generateToken(String login, UUID userId, Role role) {
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) {
            claims.put(CLAIM_USER_ID, userId.toString());
        }
        if (role != null) {
            claims.put(CLAIM_ROLE, role.name());
        }
        return Jwts.builder()
                .claims()
                .add(claims)
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

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
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

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && validateToken(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
