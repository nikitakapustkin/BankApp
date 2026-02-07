package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.Account;

import java.util.List;
import java.util.UUID;

public interface LoadAccountsPort {
    List<Account> loadAccounts(UUID userId);
}
