package org.nikitakapustkin.application.ports.in.commands;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Getter;
import org.nikitakapustkin.application.ports.in.SelfValidating;

@Getter
public class CreateAccountCommand extends SelfValidating<CreateAccountCommand> {
  @NotNull private final UUID ownerId;

  public CreateAccountCommand(UUID ownerId) {
    this.ownerId = ownerId;
    validateSelf();
  }
}
