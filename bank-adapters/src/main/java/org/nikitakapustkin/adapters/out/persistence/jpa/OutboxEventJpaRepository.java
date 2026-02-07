package org.nikitakapustkin.adapters.out.persistence.jpa;

import org.nikitakapustkin.adapters.out.persistence.jpa.entity.OutboxEventEntity;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.OutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {
    Page<OutboxEventEntity> findByStatusOrderByCreatedAt(OutboxStatus status, Pageable pageable);

    long deleteByStatusAndPublishedAtBefore(OutboxStatus status, Instant cutoff);

    long deleteByStatusAndLastAttemptAtBefore(OutboxStatus status, Instant cutoff);

    @Modifying
    @Transactional
    @Query("""
            update OutboxEventEntity e
            set e.status = :processingStatus,
                e.lastAttemptAt = :now
            where e.id = :id
              and e.status = :newStatus
            """)
    int claimForProcessing(@Param("id") UUID id,
                           @Param("newStatus") OutboxStatus newStatus,
                           @Param("processingStatus") OutboxStatus processingStatus,
                           @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("""
            update OutboxEventEntity e
            set e.status = :sentStatus,
                e.publishedAt = :publishedAt,
                e.lastError = null
            where e.id = :id
            """)
    void markSent(@Param("id") UUID id,
                 @Param("sentStatus") OutboxStatus sentStatus,
                 @Param("publishedAt") Instant publishedAt);

    @Modifying
    @Transactional
    @Query("""
            update OutboxEventEntity e
            set e.status = :newStatus,
                e.attempts = e.attempts + 1,
                e.lastAttemptAt = :now,
                e.lastError = :lastError
            where e.id = :id
            """)
    void markFailed(@Param("id") UUID id,
                   @Param("newStatus") OutboxStatus newStatus,
                   @Param("now") Instant now,
                   @Param("lastError") String lastError);
}
