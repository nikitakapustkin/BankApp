package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.WithdrawMoneyCommand;

public interface WithdrawMoneyUseCase {
  void withdraw(WithdrawMoneyCommand command);
}
