package org.nikitakapustkin.storage.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class AccountEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "event_id", columnDefinition = "uuid", unique = true)
  private UUID eventId;

  @Column(name = "correlation_id", columnDefinition = "uuid")
  private UUID correlationId;

  @Column(name = "account_id", columnDefinition = "uuid")
  private UUID accountId;

  private String eventType;

  private Instant eventTime;

  private String eventDescription;

  @Column(name = "payload_type")
  private String payloadType;

  @Column(name = "payload", columnDefinition = "text")
  private String payload;

  public AccountEvent(
      UUID eventId,
      UUID correlationId,
      UUID accountId,
      String eventType,
      Instant eventTime,
      String eventDescription,
      String payloadType,
      String payload) {
    this.eventId = eventId;
    this.correlationId = correlationId;
    this.accountId = accountId;
    this.eventType = eventType;
    this.eventTime = eventTime;
    this.eventDescription = eventDescription;
    this.payloadType = payloadType;
    this.payload = payload;
  }
}
