package org.nikitakapustkin.application.ports.in;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.nikitakapustkin.application.ports.in.commands.TransferMoneyCommand;

public class DifferentAccountsValidator
    implements ConstraintValidator<DifferentAccounts, TransferMoneyCommand> {

  @Override
  public boolean isValid(TransferMoneyCommand cmd, ConstraintValidatorContext context) {
    if (cmd == null) return true;
    if (cmd.getFromAccountId() == null || cmd.getToAccountId() == null) return true;
    return !cmd.getFromAccountId().equals(cmd.getToAccountId());
  }
}
