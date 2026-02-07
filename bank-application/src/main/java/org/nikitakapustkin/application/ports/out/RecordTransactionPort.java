package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.Transaction;

public interface RecordTransactionPort {
    Transaction recordTransaction(Transaction transaction);
}
