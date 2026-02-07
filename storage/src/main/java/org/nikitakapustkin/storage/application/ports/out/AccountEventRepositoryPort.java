package org.nikitakapustkin.storage.application.ports.out;

import org.nikitakapustkin.storage.events.AccountEvent;

public interface AccountEventRepositoryPort {
    AccountEvent save(AccountEvent event);
}
