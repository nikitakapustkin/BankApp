package org.nikitakapustkin.adapters.out.maintenance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikitakapustkin.adapters.out.persistence.jpa.OutboxEventJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.OutboxStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "maintenance.cleanup.enabled", matchIfMissing = true)
public class MaintenanceCleanupJob {
    private final OutboxEventJpaRepository outboxRepository;
    private final MaintenanceCleanupProperties properties;

    @Scheduled(cron = "#{@maintenanceCleanupProperties.cron}")
    public void cleanup() {
        Instant now = Instant.now();
        Instant outboxCutoff = now.minus(properties.getOutboxRetentionDays(), ChronoUnit.DAYS);

        long outboxDeleted = outboxRepository.deleteByStatusAndPublishedAtBefore(
                OutboxStatus.SENT,
                outboxCutoff
        );
        long failedDeleted = outboxRepository.deleteByStatusAndLastAttemptAtBefore(
                OutboxStatus.FAILED,
                outboxCutoff
        );

        log.info("Cleanup completed: outboxSentDeleted={}, outboxFailedDeleted={}",
                outboxDeleted,
                failedDeleted
        );
    }
}
