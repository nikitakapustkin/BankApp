package org.nikitakapustkin.adapters.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "outbox_events")
public class OutboxEventEntity {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Column(name = "event_key")
  private String key;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "payload", nullable = false, columnDefinition = "text")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OutboxStatus status;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "last_attempt_at")
  private Instant lastAttemptAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "last_error", columnDefinition = "text")
  private String lastError;

  public static OutboxEventEntity newEvent(
      String topic, String key, String eventType, String payload) {
    return new OutboxEventEntity(
        UUID.randomUUID(),
        topic,
        key,
        eventType,
        payload,
        OutboxStatus.NEW,
        0,
        Instant.now(),
        null,
        null,
        null);
  }
}
