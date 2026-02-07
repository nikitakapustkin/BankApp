package org.nikitakapustkin.storage.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nikitakapustkin.bank.contracts.events.AccountCreatedPayload;
import org.nikitakapustkin.bank.contracts.events.AccountDepositedPayload;
import org.nikitakapustkin.bank.contracts.events.AccountTransferredPayload;
import org.nikitakapustkin.bank.contracts.events.AccountWithdrawnPayload;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.FriendAddedPayload;
import org.nikitakapustkin.bank.contracts.events.FriendRemovedPayload;
import org.nikitakapustkin.bank.contracts.events.UnknownEventPayload;
import org.nikitakapustkin.bank.contracts.events.UserCreatedPayload;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class EventPayloadRegistry {
    private static final PayloadMapping UNKNOWN_USER_MAPPING = mapping(
            UnknownEventPayload.class,
            payload -> new EventFields(payload.entityId(), payload.description())
    );
    private static final PayloadMapping UNKNOWN_ACCOUNT_MAPPING = mapping(
            UnknownEventPayload.class,
            payload -> new EventFields(payload.entityId(), payload.description())
    );
    private static final Map<String, PayloadMapping> USER_EVENT_MAPPINGS = Map.of(
            EventTypes.USER_CREATED, mapping(
                    UserCreatedPayload.class,
                    payload -> new EventFields(payload.userId(), payload.description())
            ),
            EventTypes.FRIEND_ADDED, mapping(
                    FriendAddedPayload.class,
                    payload -> new EventFields(payload.userId(), payload.description())
            ),
            EventTypes.FRIEND_REMOVED, mapping(
                    FriendRemovedPayload.class,
                    payload -> new EventFields(payload.userId(), payload.description())
            )
    );
    private static final Map<String, PayloadMapping> ACCOUNT_EVENT_MAPPINGS = Map.of(
            EventTypes.ACCOUNT_CREATED, mapping(
                    AccountCreatedPayload.class,
                    payload -> new EventFields(payload.accountId(), payload.description())
            ),
            EventTypes.ACCOUNT_DEPOSIT, mapping(
                    AccountDepositedPayload.class,
                    payload -> new EventFields(payload.accountId(), payload.description())
            ),
            EventTypes.ACCOUNT_WITHDRAWAL, mapping(
                    AccountWithdrawnPayload.class,
                    payload -> new EventFields(payload.accountId(), payload.description())
            ),
            EventTypes.ACCOUNT_TRANSFER, mapping(
                    AccountTransferredPayload.class,
                    payload -> new EventFields(payload.accountId(), payload.description())
            )
    );

    private EventPayloadRegistry() {}

    public static PayloadInfo resolveUserPayload(String eventType, JsonNode payload, ObjectMapper objectMapper) {
        return resolvePayload(eventType, payload, objectMapper, USER_EVENT_MAPPINGS, UNKNOWN_USER_MAPPING);
    }

    public static PayloadInfo resolveAccountPayload(String eventType, JsonNode payload, ObjectMapper objectMapper) {
        return resolvePayload(eventType, payload, objectMapper, ACCOUNT_EVENT_MAPPINGS, UNKNOWN_ACCOUNT_MAPPING);
    }

    private static PayloadInfo resolvePayload(String eventType,
                                              JsonNode payload,
                                              ObjectMapper objectMapper,
                                              Map<String, PayloadMapping> mappings,
                                              PayloadMapping fallback) {
        PayloadMapping mapping = mappings.getOrDefault(eventType, fallback);
        Object parsed = readPayload(payload, mapping.payloadClass(), objectMapper);
        EventFields fields = mapping.extractor().apply(parsed);
        return new PayloadInfo(fields, mapping.payloadClass().getName());
    }

    private static <T> T readPayload(JsonNode payload, Class<T> type, ObjectMapper objectMapper) {
        if (payload == null || payload.isNull()) {
            throw new IllegalStateException("Missing payload for " + type.getSimpleName());
        }
        try {
            return objectMapper.treeToValue(payload, type);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse payload as " + type.getSimpleName(), ex);
        }
    }

    private static <T> PayloadMapping mapping(Class<T> payloadClass, Function<T, EventFields> extractor) {
        return new PayloadMapping(payloadClass, payload -> extractor.apply(payloadClass.cast(payload)));
    }

    public record EventFields(UUID entityId, String description) {}

    public record PayloadInfo(EventFields fields, String payloadType) {}

    private record PayloadMapping(Class<?> payloadClass, Function<Object, EventFields> extractor) {}
}
