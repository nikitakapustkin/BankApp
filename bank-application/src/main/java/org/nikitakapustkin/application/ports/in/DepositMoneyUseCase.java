package org.nikitakapustkin.application.ports.in;

import org.nikitakapustkin.application.ports.in.commands.DepositMoneyCommand;

public interface DepositMoneyUseCase {
    void deposit(DepositMoneyCommand command);
}