package org.nikitakapustkin.application.ports.out;

import org.nikitakapustkin.domain.models.Account;

public interface CreateAccountPort {
    Account createAccount(Account account);
}
