package org.nikitakapustkin.adapters.out.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikitakapustkin.adapters.out.persistence.jpa.OutboxEventJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.OutboxEventEntity;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.OutboxStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "outbox.publisher.enabled", matchIfMissing = true)
public class OutboxKafkaPublisher {
    private final OutboxEventJpaRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxPublisherProperties properties;

    @Scheduled(fixedDelayString = "#{@outboxPublisherProperties.intervalMs}")
    public void publishBatch() {
        var page = outboxRepository.findByStatusOrderByCreatedAt(
                OutboxStatus.NEW,
                PageRequest.of(0, properties.getBatchSize())
        );

        for (OutboxEventEntity event : page.getContent()) {
            if (outboxRepository.claimForProcessing(
                    event.getId(),
                    OutboxStatus.NEW,
                    OutboxStatus.PROCESSING,
                    Instant.now()
            ) == 0) {
                continue;
            }

            int maxAttempts = properties.getMaxAttempts();
            boolean enforceMaxAttempts = maxAttempts > 0;
            if (enforceMaxAttempts && event.getAttempts() >= maxAttempts) {
                outboxRepository.markFailed(
                        event.getId(),
                        OutboxStatus.FAILED,
                        Instant.now(),
                        "Max attempts exceeded"
                );
                log.warn("Outbox event {} moved to FAILED after {} attempts", event.getId(), event.getAttempts());
                continue;
            }

            try {
                kafkaTemplate.send(event.getTopic(), event.getKey(), event.getPayload())
                        .get(properties.getPublishTimeoutMs(), TimeUnit.MILLISECONDS);

                outboxRepository.markSent(event.getId(), OutboxStatus.SENT, Instant.now());
            } catch (Exception ex) {
                String message = ex.getMessage();
                if (message != null && message.length() > 2000) {
                    message = message.substring(0, 2000);
                }
                boolean terminal = enforceMaxAttempts && event.getAttempts() + 1 >= maxAttempts;
                OutboxStatus nextStatus = terminal ? OutboxStatus.FAILED : OutboxStatus.NEW;
                outboxRepository.markFailed(event.getId(), nextStatus, Instant.now(), message);
                log.warn("Failed to publish outbox event {} to {} (attempt {}/{}): {}",
                        event.getId(),
                        event.getTopic(),
                        event.getAttempts() + 1,
                        enforceMaxAttempts ? maxAttempts : -1,
                        ex.toString()
                );
            }
        }
    }
}
