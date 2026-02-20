package org.nikitakapustkin.application.ports.in.queries;

import java.util.UUID;
import org.nikitakapustkin.domain.models.Account;

public interface GetAccountQuery {
  Account getAccount(UUID accountId);
}
