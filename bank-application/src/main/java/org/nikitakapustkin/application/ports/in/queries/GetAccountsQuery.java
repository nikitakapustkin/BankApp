package org.nikitakapustkin.application.ports.in.queries;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.domain.models.Account;

public interface GetAccountsQuery {
  List<Account> getAccounts(UUID userId);
}
