package org.nikitakapustkin.application.services.queries;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.queries.GetAccountsQuery;
import org.nikitakapustkin.application.ports.out.LoadAccountsPort;
import org.nikitakapustkin.domain.models.Account;

@RequiredArgsConstructor
public class GetAccountsQueryService implements GetAccountsQuery {

  private final LoadAccountsPort loadAccountsPort;

  @Override
  public List<Account> getAccounts(UUID userId) {
    return loadAccountsPort.loadAccounts(userId);
  }
}
