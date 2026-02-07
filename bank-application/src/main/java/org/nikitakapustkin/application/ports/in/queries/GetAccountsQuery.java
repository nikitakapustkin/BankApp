package org.nikitakapustkin.application.ports.in.queries;

import org.nikitakapustkin.domain.models.Account;

import java.util.List;
import java.util.UUID;

public interface GetAccountsQuery {
    List<Account> getAccounts(UUID userId);
}
