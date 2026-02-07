package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.CreateAccountCommand;
import org.nikitakapustkin.domain.models.Account;

public interface CreateAccountUseCase {
    Account createAccount(CreateAccountCommand command);
}