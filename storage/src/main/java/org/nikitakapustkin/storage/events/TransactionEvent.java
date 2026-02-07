package org.nikitakapustkin.storage.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "event_id", columnDefinition = "uuid", unique = true)
    private UUID eventId;

    @Column(name = "transaction_id", columnDefinition = "uuid", unique = true)
    private UUID transactionId;

    @Column(name = "correlation_id", columnDefinition = "uuid")
    private UUID correlationId;

    @Column(name = "account_id", columnDefinition = "uuid")
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private BigDecimal amount;

    private Instant createdAt;

    private String eventType;

    private Instant eventTime;

    private String eventDescription;

    @Column(name = "payload_type")
    private String payloadType;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    public TransactionEvent(UUID eventId,
                            UUID transactionId,
                            UUID correlationId,
                            UUID accountId,
                            TransactionType transactionType,
                            BigDecimal amount,
                            Instant createdAt,
                            String eventType,
                            Instant eventTime,
                            String eventDescription,
                            String payloadType,
                            String payload) {
        this.eventId = eventId;
        this.transactionId = transactionId;
        this.correlationId = correlationId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.createdAt = createdAt;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.eventDescription = eventDescription;
        this.payloadType = payloadType;
        this.payload = payload;
    }
}
