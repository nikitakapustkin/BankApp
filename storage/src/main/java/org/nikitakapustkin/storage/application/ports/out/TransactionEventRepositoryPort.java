package org.nikitakapustkin.storage.application.ports.out;

import org.nikitakapustkin.storage.events.TransactionEvent;

public interface TransactionEventRepositoryPort {
  TransactionEvent save(TransactionEvent event);
}
