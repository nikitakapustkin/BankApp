package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.TransferMoneyCommand;

public interface TransferMoneyUseCase {
  void transferMoney(TransferMoneyCommand command);
}
