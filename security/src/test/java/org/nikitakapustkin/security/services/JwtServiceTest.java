package org.nikitakapustkin.security.services;

import org.junit.jupiter.api.Test;
import org.nikitakapustkin.security.adapters.out.jwt.JwtService;
import org.nikitakapustkin.security.enums.Role;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String TEST_SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Test
    void generate_and_validate_token() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", TEST_SECRET);
        service.validateSecret();

        UUID userId = UUID.randomUUID();
        String token = service.generateToken("alice", userId, Role.CLIENT);

        assertThat(service.extractUsername(token)).isEqualTo("alice");
        assertThat(service.extractUserId(token)).isEqualTo(userId);
        assertThat(service.extractRole(token)).isEqualTo("CLIENT");
        UserDetails userDetails = User.withUsername("alice")
                .password("encoded")
                .roles("CLIENT")
                .build();
        assertThat(service.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void validate_token_fails_for_other_user() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", TEST_SECRET);
        service.validateSecret();

        String token = service.generateToken("alice", UUID.randomUUID(), Role.CLIENT);

        UserDetails other = User.withUsername("bob")
                .password("encoded")
                .roles("CLIENT")
                .build();
        assertThat(service.validateToken(token, other)).isFalse();
    }

    @Test
    void validate_secret_throws_when_missing() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", " ");

        assertThatThrownBy(service::validateSecret)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret");
    }
}
