package org.nikitakapustkin.security.adapters.out.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.nikitakapustkin.security.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class ServiceJwtService {
    private static final String CLAIM_ROLE = "role";

    @Value("${jwt.service.secret}")
    private String secret;

    @Value("${jwt.service.issuer}")
    private String issuer;

    @Value("${jwt.service.audience}")
    private String audience;

    @PostConstruct
    void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.service.secret is not set.");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("jwt.service.issuer is not set.");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException("jwt.service.audience is not set.");
        }
    }

    public String generateToken(String subject, Role role) {
        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(subject)
                .claim(CLAIM_ROLE, role != null ? role.name() : null)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] encodedKey = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(encodedKey);
    }
}
