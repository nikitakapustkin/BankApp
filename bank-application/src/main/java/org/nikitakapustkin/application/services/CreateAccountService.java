package org.nikitakapustkin.application.services;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.CreateAccountUseCase;
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

@RequiredArgsConstructor
public class CreateAccountService implements CreateAccountUseCase {

  private final LoadUserPort loadUserPort;
  private final CreateAccountPort createAccountPort;
  private final PublishAccountEventPort publishAccountEventPort;

  @Override
  public Account createAccount(CreateAccountCommand command) {
    User owner =
        loadUserPort
            .loadUserById(command.getOwnerId())
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "User with ID " + command.getOwnerId() + " not found"));

    Account account =
        Account.builder()
            .userId(owner.getId())
            .ownerLogin(owner.getLogin())
            .balance(BigDecimal.ZERO)
            .build();

    var created = createAccountPort.createAccount(account);
    String description = "Account created for user " + created.getUserId();
    publishAccountEventPort.publishAccountEvent(
        DomainEvent.now(
            created.getId(),
            EventType.ACCOUNT_CREATED,
            description,
            created.getUserId(),
            new AccountCreatedEventData(
                created.getId(), created.getUserId(), created.getOwnerLogin(), description)));
    return created;
  }
}
