package org.nikitakapustkin.application.ports.in.queries;

import org.nikitakapustkin.domain.models.Account;

import java.util.UUID;

public interface GetAccountQuery {
    Account getAccount(UUID accountId);
}
