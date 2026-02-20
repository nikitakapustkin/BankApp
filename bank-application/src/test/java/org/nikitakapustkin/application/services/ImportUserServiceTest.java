package org.nikitakapustkin.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.application.ports.in.commands.ImportUserCommand;
import org.nikitakapustkin.application.ports.out.CreateUserPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;
import org.nikitakapustkin.domain.models.User;

@ExtendWith(MockitoExtension.class)
class ImportUserServiceTest {

  @Mock LoadUserPort loadUserPort;
  @Mock CreateUserPort createUserPort;

  @InjectMocks ImportUserService service;

  @Test
  void import_user_creates_when_missing() {
    UUID userId = UUID.randomUUID();
    ImportUserCommand cmd =
        new ImportUserCommand(userId, "alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK);

    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.empty());
    when(loadUserPort.loadUserByLogin("alice")).thenReturn(Optional.empty());

    User created =
        User.builder()
            .id(userId)
            .login("alice")
            .name("Alice")
            .age(25)
            .sex(Sex.FEMALE)
            .hairColor(HairColor.BLACK)
            .build();
    when(createUserPort.create(any(User.class))).thenReturn(created);

    User result = service.importUser(cmd);

    assertThat(result).isEqualTo(created);
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(createUserPort).create(userCaptor.capture());
    assertThat(userCaptor.getValue().getId()).isEqualTo(userId);
    assertThat(userCaptor.getValue().getLogin()).isEqualTo("alice");
  }

  @Test
  void import_user_returns_existing_by_id() {
    UUID userId = UUID.randomUUID();
    ImportUserCommand cmd =
        new ImportUserCommand(userId, "alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK);

    User existing = User.builder().id(userId).login("alice").build();
    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.of(existing));

    User result = service.importUser(cmd);

    assertThat(result).isEqualTo(existing);
    verifyNoInteractions(createUserPort);
  }

  @Test
  void import_user_returns_existing_by_login() {
    UUID userId = UUID.randomUUID();
    ImportUserCommand cmd =
        new ImportUserCommand(userId, "alice", "Alice", 25, Sex.FEMALE, HairColor.BLACK);

    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.empty());
    User existing = User.builder().id(UUID.randomUUID()).login("alice").build();
    when(loadUserPort.loadUserByLogin("alice")).thenReturn(Optional.of(existing));

    User result = service.importUser(cmd);

    assertThat(result).isEqualTo(existing);
    verifyNoInteractions(createUserPort);
  }
}
