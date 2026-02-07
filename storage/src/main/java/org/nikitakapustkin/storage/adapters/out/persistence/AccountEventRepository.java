package org.nikitakapustkin.storage.adapters.out.persistence;

import org.nikitakapustkin.storage.application.ports.out.AccountEventRepositoryPort;
import org.nikitakapustkin.storage.events.AccountEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountEventRepository extends JpaRepository<AccountEvent, UUID>, AccountEventRepositoryPort {
    @Query("""
            select e
            from AccountEvent e
            where (:eventType is null or lower(e.eventType) = :eventType)
              and (:accountId is null or e.accountId = :accountId)
              and (:correlationId is null or e.correlationId = :correlationId)
            order by e.eventTime desc, e.id desc
            """)
    List<AccountEvent> findEvents(
            @Param("eventType") String eventType,
            @Param("accountId") UUID accountId,
            @Param("correlationId") UUID correlationId,
            Pageable pageable
    );
}
