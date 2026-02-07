package org.nikitakapustkin.adapters.out.persistence.jpa;

import org.nikitakapustkin.adapters.out.persistence.jpa.entity.TransactionEntity;
import org.nikitakapustkin.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByTransactionType(TransactionType type);
    List<TransactionEntity> findByAccount_AccountId(UUID accountId);
    List<TransactionEntity> findByTransactionTypeAndAccount_AccountId(TransactionType type, UUID accountId);
}
