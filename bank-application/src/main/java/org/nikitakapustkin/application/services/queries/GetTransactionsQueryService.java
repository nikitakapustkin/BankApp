package org.nikitakapustkin.application.services.queries;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.queries.GetTransactionsQuery;
import org.nikitakapustkin.application.ports.out.LoadTransactionsPort;
import org.nikitakapustkin.domain.enums.TransactionType;
import org.nikitakapustkin.domain.models.Transaction;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class GetTransactionsQueryService implements GetTransactionsQuery {

    private final LoadTransactionsPort loadTransactionsPort;

    @Override
    public List<Transaction> getTransactions(String typeStr, UUID accountId) {
        TransactionType type = null;
        if (typeStr != null && !typeStr.isBlank()) {
            type = TransactionType.valueOf(typeStr.trim().toUpperCase());
        }

        if (accountId != null) {
            return (type == null)
                    ? loadTransactionsPort.loadByAccountId(accountId)
                    : loadTransactionsPort.loadByAccountIdAndType(accountId, type);
        }

        return (type == null)
                ? loadTransactionsPort.loadAll()
                : loadTransactionsPort.loadByType(type);
    }

}
