package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.Account;

import java.util.Optional;
import java.util.UUID;

public interface LoadAccountPort {
    Optional<Account> loadAccount(UUID accountId);
}
