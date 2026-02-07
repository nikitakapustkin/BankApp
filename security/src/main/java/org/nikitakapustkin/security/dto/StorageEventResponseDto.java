package org.nikitakapustkin.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
