package org.nikitakapustkin.storage.adapters.out.persistence;

import org.nikitakapustkin.storage.application.ports.out.UserEventRepositoryPort;
import org.nikitakapustkin.storage.events.UserEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, UUID>, UserEventRepositoryPort {
    @Query("""
            select e
            from UserEvent e
            where (:eventType is null or lower(e.eventType) = :eventType)
              and (:userId is null or e.userId = :userId)
              and (:correlationId is null or e.correlationId = :correlationId)
            order by e.eventTime desc, e.id desc
            """)
    List<UserEvent> findEvents(
            @Param("eventType") String eventType,
            @Param("userId") UUID userId,
            @Param("correlationId") UUID correlationId,
            Pageable pageable
    );
}
