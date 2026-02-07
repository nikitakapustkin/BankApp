package org.nikitakapustkin.security.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.security.application.AuthenticationService;
import org.nikitakapustkin.security.application.models.AuthenticatedUser;
import org.nikitakapustkin.security.application.ports.out.CredentialsAuthenticatorPort;
import org.nikitakapustkin.security.application.ports.out.JwtIssuerPort;
import org.nikitakapustkin.security.dto.LoginRequestDto;
import org.nikitakapustkin.security.enums.Role;
import org.nikitakapustkin.security.exceptions.AuthenticationFailedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock CredentialsAuthenticatorPort authenticator;
    @Mock JwtIssuerPort jwtService;

    @InjectMocks AuthenticationService service;

    @Test
    void authenticate_returns_token_when_authenticated() {
        LoginRequestDto request = new LoginRequestDto("alice", "pass");
        UUID userId = UUID.randomUUID();
        when(authenticator.authenticate("alice", "pass"))
                .thenReturn(new AuthenticatedUser("alice", userId, Role.CLIENT));
        when(jwtService.generateToken("alice", userId, Role.CLIENT)).thenReturn("token");

        String token = service.authenticate(request);

        assertThat(token).isEqualTo("token");
        verify(jwtService).generateToken("alice", userId, Role.CLIENT);
    }

    @Test
    void authenticate_throws_when_not_authenticated() {
        LoginRequestDto request = new LoginRequestDto("alice", "pass");
        when(authenticator.authenticate("alice", "pass")).thenReturn(null);

        assertThatThrownBy(() -> service.authenticate(request))
                .isInstanceOf(AuthenticationFailedException.class);

        verifyNoInteractions(jwtService);
    }
}
