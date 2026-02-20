package org.nikitakapustkin.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.application.ports.in.commands.CreateAccountCommand;
import org.nikitakapustkin.application.ports.out.CreateAccountPort;
import org.nikitakapustkin.application.ports.out.LoadUserPort;
import org.nikitakapustkin.application.ports.out.PublishAccountEventPort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.AccountCreatedEventData;
import org.nikitakapustkin.domain.exceptions.UserNotFoundException;
import org.nikitakapustkin.domain.models.Account;
import org.nikitakapustkin.domain.models.User;

@ExtendWith(MockitoExtension.class)
class CreateAccountServiceTest {

  @Mock LoadUserPort loadUserPort;
  @Mock CreateAccountPort createAccountPort;
  @Mock PublishAccountEventPort publishAccountEventPort;

  @InjectMocks CreateAccountService service;

  @Captor ArgumentCaptor<DomainEvent> eventCaptor;

  @Test
  void create_account_publishes_event() {
    UUID userId = UUID.randomUUID();
    User owner = User.builder().id(userId).login("alice").build();
    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.of(owner));

    UUID accountId = UUID.randomUUID();
    Account created =
        Account.builder()
            .id(accountId)
            .userId(userId)
            .ownerLogin("alice")
            .balance(BigDecimal.ZERO)
            .build();
    when(createAccountPort.createAccount(any(Account.class))).thenReturn(created);

    Account result = service.createAccount(new CreateAccountCommand(userId));

    assertThat(result).isEqualTo(created);
    verify(publishAccountEventPort).publishAccountEvent(eventCaptor.capture());

    DomainEvent event = eventCaptor.getValue();
    assertThat(event.getEntityId()).isEqualTo(accountId);
    assertThat(event.getCorrelationId()).isEqualTo(userId);
    assertThat(event.getEventType()).isEqualTo(EventType.ACCOUNT_CREATED);
    assertThat(event.getEventTime()).isNotNull();
    assertThat(event.getEventDescription()).contains(userId.toString());
    assertThat(event.getPayload()).isInstanceOf(AccountCreatedEventData.class);
    AccountCreatedEventData payload = (AccountCreatedEventData) event.getPayload();
    assertThat(payload.accountId()).isEqualTo(accountId);
    assertThat(payload.ownerId()).isEqualTo(userId);
    assertThat(payload.ownerLogin()).isEqualTo("alice");
    assertThat(payload.description()).contains(userId.toString());
  }

  @Test
  void create_account_throws_when_user_missing() {
    UUID userId = UUID.randomUUID();
    when(loadUserPort.loadUserById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.createAccount(new CreateAccountCommand(userId)))
        .isInstanceOf(UserNotFoundException.class);

    verifyNoInteractions(createAccountPort);
    verifyNoInteractions(publishAccountEventPort);
  }
}
