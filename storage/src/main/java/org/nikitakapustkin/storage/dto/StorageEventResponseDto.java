package org.nikitakapustkin.storage.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Value;

@Value
public class StorageEventResponseDto {
  UUID eventId;
  UUID correlationId;
  String source;
  UUID entityId;
  UUID transactionId;
  String transactionType;
  BigDecimal amount;
  Instant createdAt;
  String eventType;
  Instant eventTime;
  String eventDescription;
  String payloadType;
  String payload;
}
