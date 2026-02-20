package org.nikitakapustkin.application.ports.in.queries;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.domain.models.Transaction;

public interface GetTransactionsQuery {
  List<Transaction> getTransactions(String type, UUID accountId);
}
