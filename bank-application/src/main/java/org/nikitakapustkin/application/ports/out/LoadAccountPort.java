package org.nikitakapustkin.application.ports.out;

import java.util.Optional;
import java.util.UUID;
import org.nikitakapustkin.domain.models.Account;

public interface LoadAccountPort {
  Optional<Account> loadAccount(UUID accountId);
}
