package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.CreateUserCommand;
import org.nikitakapustkin.domain.models.User;

public interface CreateUserUseCase {
    User createUser(CreateUserCommand command);
}