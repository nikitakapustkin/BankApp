package org.nikitakapustkin.storage.adapters.out.persistence;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.storage.application.ports.out.TransactionEventRepositoryPort;
import org.nikitakapustkin.storage.events.TransactionEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionEventRepository
    extends JpaRepository<TransactionEvent, UUID>, TransactionEventRepositoryPort {
  @Query(
      """
            select e
            from TransactionEvent e
            where (:eventType is null or lower(e.eventType) = :eventType)
              and (:accountId is null or e.accountId = :accountId)
              and (:correlationId is null or e.correlationId = :correlationId)
              and (:transactionType is null or e.transactionType = :transactionType)
            order by e.eventTime desc, e.id desc
            """)
  List<TransactionEvent> findEvents(
      @Param("eventType") String eventType,
      @Param("accountId") UUID accountId,
      @Param("correlationId") UUID correlationId,
      @Param("transactionType") TransactionType transactionType,
      Pageable pageable);
}
