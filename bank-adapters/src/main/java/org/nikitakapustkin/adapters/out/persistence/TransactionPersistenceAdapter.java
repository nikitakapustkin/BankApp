package org.nikitakapustkin.adapters.out.persistence;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.domain.enums.TransactionType;
import org.nikitakapustkin.domain.models.Transaction;
import org.nikitakapustkin.application.ports.out.LoadTransactionsPort;
import org.nikitakapustkin.application.ports.out.RecordTransactionPort;
import org.nikitakapustkin.adapters.out.persistence.mapper.TransactionMapper;
import org.nikitakapustkin.adapters.out.persistence.jpa.AccountJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.jpa.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements RecordTransactionPort, LoadTransactionsPort {

    private final TransactionJpaRepository transactions;
    private final AccountJpaRepository accounts;

    @Override
    public Transaction recordTransaction(Transaction transaction) {
        var accountRef = accounts.getReferenceById(transaction.getAccountId());
        var entity = TransactionMapper.toJpaEntity(transaction, accountRef);
        var saved = transactions.save(entity);
        return TransactionMapper.toDomain(saved);
    }

    @Override
    public List<Transaction> loadAll() {
        return transactions.findAll().stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> loadByType(TransactionType type) {
        return transactions.findByTransactionType(type).stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> loadByAccountId(UUID accountId) {
        return transactions.findByAccount_AccountId(accountId).stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> loadByAccountIdAndType(UUID accountId, TransactionType type) {
        return transactions.findByTransactionTypeAndAccount_AccountId(type, accountId).stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }

}
