package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.Account;

public interface UpdateAccountStatePort {
  void updateAccount(Account account);
}
