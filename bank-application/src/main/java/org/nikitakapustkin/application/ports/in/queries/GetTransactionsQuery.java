package org.nikitakapustkin.application.ports.in.queries;

import org.nikitakapustkin.domain.models.Transaction;

import java.util.List;
import java.util.UUID;

public interface GetTransactionsQuery {
    List<Transaction> getTransactions(String type, UUID accountId);
}
