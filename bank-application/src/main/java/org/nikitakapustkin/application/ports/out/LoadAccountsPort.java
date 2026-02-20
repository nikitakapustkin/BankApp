package org.nikitakapustkin.application.ports.out;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.domain.models.Account;

public interface LoadAccountsPort {
  List<Account> loadAccounts(UUID userId);
}
