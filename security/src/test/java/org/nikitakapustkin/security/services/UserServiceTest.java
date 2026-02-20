package org.nikitakapustkin.security.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.security.application.UserService;
import org.nikitakapustkin.security.application.ports.out.PasswordHasherPort;
import org.nikitakapustkin.security.application.ports.out.UserBankClientPort;
import org.nikitakapustkin.security.application.ports.out.UserEventPublisherPort;
import org.nikitakapustkin.security.application.ports.out.UserRepositoryPort;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;
import org.nikitakapustkin.security.enums.Role;
import org.nikitakapustkin.security.exceptions.UserAlreadyExistsException;
import org.nikitakapustkin.security.models.User;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock UserRepositoryPort userRepository;
  @Mock UserBankClientPort userWebClient;
  @Mock PasswordHasherPort passwordEncoder;
  @Mock UserEventPublisherPort userEventOutboxService;

  @InjectMocks UserService service;

  @Test
  void create_user_persists_user_and_enqueues_event() {
    UserCreateRequestDto dto =
        new UserCreateRequestDto("alice", "pass", "Alice", "FEMALE", "BLACK", 25);
    when(userRepository.existsByLogin("alice")).thenReturn(false);
    when(passwordEncoder.encode("pass")).thenReturn("encoded");

    service.createUser(dto);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();

    assertThat(saved.getUserId()).isNotNull();
    assertThat(saved.getLogin()).isEqualTo("alice");
    assertThat(saved.getPassword()).isEqualTo("encoded");
    assertThat(saved.getRole()).isEqualTo(Role.CLIENT);

    ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(userEventOutboxService).enqueueUserCreated(eq(dto), userIdCaptor.capture());
    assertThat(userIdCaptor.getValue()).isEqualTo(saved.getUserId());
    verifyNoInteractions(userWebClient);
  }

  @Test
  void create_user_throws_when_login_exists() {
    UserCreateRequestDto dto =
        new UserCreateRequestDto("alice", "pass", "Alice", "FEMALE", "BLACK", 25);
    when(userRepository.existsByLogin("alice")).thenReturn(true);

    assertThatThrownBy(() -> service.createUser(dto))
        .isInstanceOf(UserAlreadyExistsException.class);

    verifyNoInteractions(userWebClient);
    verifyNoInteractions(userEventOutboxService);
    verify(userRepository, never()).save(any());
  }

  @Test
  void create_user_throws_when_age_invalid() {
    UserCreateRequestDto dto =
        new UserCreateRequestDto("alice", "pass", "Alice", "FEMALE", "BLACK", 0);
    when(userRepository.existsByLogin("alice")).thenReturn(false);

    assertThatThrownBy(() -> service.createUser(dto)).isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(userWebClient);
    verifyNoInteractions(userEventOutboxService);
    verify(userRepository, never()).save(any());
  }
}
