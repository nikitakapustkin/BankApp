package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.ImportUserCommand;
import org.nikitakapustkin.domain.models.User;

public interface ImportUserUseCase {
  User importUser(ImportUserCommand command);
}
