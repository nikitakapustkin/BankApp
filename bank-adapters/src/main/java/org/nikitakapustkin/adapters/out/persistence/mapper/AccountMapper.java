package org.nikitakapustkin.adapters.out.persistence.mapper;


import org.nikitakapustkin.adapters.out.persistence.jpa.entity.AccountEntity;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.UserEntity;
import org.nikitakapustkin.domain.models.Account;

public final class AccountMapper {
    private AccountMapper() {}

    public static Account toDomain(AccountEntity e) {
        if (e == null) return null;

        return Account.builder()
                .id(e.getAccountId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .ownerLogin(e.getOwnerLogin())
                .balance(e.getBalance())
                .build();
    }

    // CREATE: тут user обязателен, поэтому передаём userRef
    public static AccountEntity toJpaEntity(
            Account d,
            UserEntity userRef
    ) {
        if (d == null) return null;

        var e = new AccountEntity();
        e.setUser(userRef);
        e.setOwnerLogin(d.getOwnerLogin());
        e.setBalance(d.getBalance());
        return e;
    }

    // UPDATE: user не меняем
    public static void applyToJpaEntity(Account d, AccountEntity e) {
        if (d == null || e == null) return;

        e.setOwnerLogin(d.getOwnerLogin());
        e.setBalance(d.getBalance());
    }
}
