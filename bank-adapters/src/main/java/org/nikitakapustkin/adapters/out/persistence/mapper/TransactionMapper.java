package org.nikitakapustkin.adapters.out.persistence.mapper;


import org.nikitakapustkin.adapters.out.persistence.jpa.entity.AccountEntity;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.TransactionEntity;
import org.nikitakapustkin.domain.models.Transaction;

public final class TransactionMapper {
    private TransactionMapper() {}

    public static Transaction toDomain(TransactionEntity e) {
        if (e == null) return null;

        return Transaction.builder()
                .id(e.getId())
                .transactionType(e.getTransactionType())
                .accountId(e.getAccount() != null ? e.getAccount().getAccountId() : null)
                .amount(e.getAmount())
                .createdAt(e.getCreatedAt())
                .build();
    }


    public static TransactionEntity toJpaEntity(
            Transaction d,
            AccountEntity account
    ) {
        if (d == null) return null;

        var e = new TransactionEntity();
        e.setAccount(account);
        e.setTransactionType(d.getTransactionType());
        e.setAmount(d.getAmount());
        e.setCreatedAt(d.getCreatedAt());
        return e;
    }
}
