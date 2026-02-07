package org.nikitakapustkin.storage.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.TransactionCreatedPayload;
import org.nikitakapustkin.storage.application.ports.out.AccountEventRepositoryPort;
import org.nikitakapustkin.storage.application.ports.out.TransactionEventRepositoryPort;
import org.nikitakapustkin.storage.application.ports.out.UserEventRepositoryPort;
import org.nikitakapustkin.storage.events.AccountEvent;
import org.nikitakapustkin.storage.events.TransactionEvent;
import org.nikitakapustkin.storage.events.UserEvent;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class EventIngestionService {
    private final UserEventRepositoryPort userEventRepository;
    private final AccountEventRepositoryPort accountEventRepository;
    private final TransactionEventRepositoryPort transactionEventRepository;
    private final ObjectMapper objectMapper;

    public void consumeUserEvents(String payload) {
        consumeEvent(
                payload,
                new TypeReference<>() {
                },
                EventEnvelope::eventId,
                this::toUserEvent,
                userEventRepository::save,
                "user",
                "eventId"
        );
    }

    public void consumeAccountEvents(String payload) {
        consumeEvent(
                payload,
                new TypeReference<>() {
                },
                EventEnvelope::eventId,
                this::toAccountEvent,
                accountEventRepository::save,
                "account",
                "eventId"
        );
    }

    public void consumeTransactionEvents(String payload) {
        consumeEvent(
                payload,
                new TypeReference<>() {
                },
                envelope -> envelope.payload() != null ? envelope.payload().transactionId() : null,
                this::toTransactionEvent,
                transactionEventRepository::save,
                "transaction",
                "transactionId"
        );
    }

    private static UUID fallbackCorrelationId(UUID eventId, UUID correlationId) {
        if (correlationId != null) {
            return correlationId;
        }
        if (eventId != null) {
            return eventId;
        }
        return UUID.randomUUID();
    }

    private <T, E> void consumeEvent(
            String payload,
            TypeReference<EventEnvelope<T>> typeRef,
            Function<EventEnvelope<T>, UUID> idExtractor,
            Function<EventEnvelope<T>, E> mapper,
            Consumer<E> saver,
            String eventName,
            String idName
    ) {
        UUID eventId = null;
        try {
            EventEnvelope<T> envelope = objectMapper.readValue(payload, typeRef);
            eventId = idExtractor.apply(envelope);
            E event = mapper.apply(envelope);
            saver.accept(event);
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate {} event ignored. {}={}", eventName, idName, eventId);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse " + eventName + " event envelope", e);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process " + eventName + " event: " + e.getMessage(), e);
        }
    }

    private UserEvent toUserEvent(EventEnvelope<JsonNode> envelope) {
        UUID eventId = envelope.eventId();
        UUID correlationId = fallbackCorrelationId(eventId, envelope.correlationId());
        String eventType = envelope.eventType() != null ? envelope.eventType() : EventTypes.UNKNOWN;
        EventPayloadRegistry.PayloadInfo payloadInfo = EventPayloadRegistry.resolveUserPayload(
                eventType,
                envelope.payload(),
                objectMapper
        );
        EventPayloadRegistry.EventFields fields = payloadInfo.fields();
        UUID userId = fields != null ? fields.entityId() : null;
        String description = fields != null ? fields.description() : null;
        String payloadJson = serializePayload(envelope.payload());

        return new UserEvent(
                eventId,
                correlationId,
                userId,
                eventType,
                envelope.occurredAt(),
                description,
                payloadInfo.payloadType(),
                payloadJson
        );
    }

    private AccountEvent toAccountEvent(EventEnvelope<JsonNode> envelope) {
        UUID eventId = envelope.eventId();
        UUID correlationId = fallbackCorrelationId(eventId, envelope.correlationId());
        String eventType = envelope.eventType() != null ? envelope.eventType() : EventTypes.UNKNOWN;
        EventPayloadRegistry.PayloadInfo payloadInfo = EventPayloadRegistry.resolveAccountPayload(
                eventType,
                envelope.payload(),
                objectMapper
        );
        EventPayloadRegistry.EventFields fields = payloadInfo.fields();
        UUID accountId = fields != null ? fields.entityId() : null;
        String description = fields != null ? fields.description() : null;
        String payloadJson = serializePayload(envelope.payload());

        return new AccountEvent(
                eventId,
                correlationId,
                accountId,
                eventType,
                envelope.occurredAt(),
                description,
                payloadInfo.payloadType(),
                payloadJson
        );
    }

    private TransactionEvent toTransactionEvent(EventEnvelope<TransactionCreatedPayload> envelope) {
        TransactionCreatedPayload payload = envelope.payload();
        UUID eventId = envelope.eventId();
        UUID correlationId = fallbackCorrelationId(eventId, envelope.correlationId());
        String eventType = envelope.eventType() != null ? envelope.eventType() : EventTypes.UNKNOWN;
        Instant eventTime = envelope.occurredAt() != null
                ? envelope.occurredAt()
                : (payload != null ? payload.createdAt() : null);
        String payloadType = payload != null ? payload.getClass().getName() : null;
        String payloadJson = serializePayload(payload);

        return new TransactionEvent(
                eventId,
                payload != null ? payload.transactionId() : null,
                correlationId,
                payload != null ? payload.accountId() : null,
                payload != null ? payload.transactionType() : null,
                payload != null ? payload.amount() : null,
                payload != null ? payload.createdAt() : null,
                eventType,
                eventTime,
                null,
                payloadType,
                payloadJson
        );
    }

    private String serializePayload(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event payload", e);
        }
    }
}
