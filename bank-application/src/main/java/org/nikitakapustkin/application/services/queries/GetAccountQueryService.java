package org.nikitakapustkin.application.services.queries;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.queries.GetAccountQuery;
import org.nikitakapustkin.application.ports.out.LoadAccountPort;
import org.nikitakapustkin.domain.exceptions.AccountNotFoundException;
import org.nikitakapustkin.domain.models.Account;

import java.util.UUID;

@RequiredArgsConstructor
public class GetAccountQueryService implements GetAccountQuery {

    private final LoadAccountPort loadAccountPort;

    @Override
    public Account getAccount(UUID accountId) {
        return loadAccountPort.loadAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
    }
}
