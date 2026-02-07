package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.enums.TransactionType;
import org.nikitakapustkin.domain.models.Transaction;

import java.util.List;
import java.util.UUID;

public interface LoadTransactionsPort {
    List<Transaction> loadAll();
    List<Transaction> loadByType(TransactionType type);
    List<Transaction> loadByAccountId(UUID accountId);
    List<Transaction> loadByAccountIdAndType(UUID accountId, TransactionType type);
}
