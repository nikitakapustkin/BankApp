package org.nikitakapustkin.adapters.out.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.adapters.out.persistence.jpa.OutboxEventJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.jpa.entity.OutboxEventEntity;
import org.nikitakapustkin.application.ports.out.PublishAccountEventPort;
import org.nikitakapustkin.application.ports.out.PublishTransactionEventPort;
import org.nikitakapustkin.application.ports.out.PublishUserEventPort;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.bank.contracts.events.AccountCreatedPayload;
import org.nikitakapustkin.bank.contracts.events.AccountDepositedPayload;
import org.nikitakapustkin.bank.contracts.events.AccountTransferredPayload;
import org.nikitakapustkin.bank.contracts.events.AccountWithdrawnPayload;
import org.nikitakapustkin.bank.contracts.events.EventEnvelope;
import org.nikitakapustkin.bank.contracts.events.EventTypes;
import org.nikitakapustkin.bank.contracts.events.FriendAddedPayload;
import org.nikitakapustkin.bank.contracts.events.FriendRemovedPayload;
import org.nikitakapustkin.bank.contracts.events.TransactionCreatedPayload;
import org.nikitakapustkin.bank.contracts.events.TransferDirection;
import org.nikitakapustkin.bank.contracts.events.UnknownEventPayload;
import org.nikitakapustkin.bank.contracts.events.UserCreatedPayload;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.AccountCreatedEventData;
import org.nikitakapustkin.domain.events.payload.AccountDepositedEventData;
import org.nikitakapustkin.domain.events.payload.AccountTransferredEventData;
import org.nikitakapustkin.domain.events.payload.AccountWithdrawnEventData;
import org.nikitakapustkin.domain.events.payload.DomainEventData;
import org.nikitakapustkin.domain.events.payload.FriendAddedEventData;
import org.nikitakapustkin.domain.events.payload.FriendRemovedEventData;
import org.nikitakapustkin.domain.events.payload.TransactionCreatedEventData;
import org.nikitakapustkin.domain.events.payload.UserCreatedEventData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxDomainEventPublisherAdapter
        implements PublishUserEventPort, PublishAccountEventPort, PublishTransactionEventPort {

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.user}")
    private String userTopic;

    @Value("${kafka.topics.account}")
    private String accountTopic;

    @Value("${kafka.topics.transaction:transaction-created-topic}")
    private String transactionTopic;

    private static final String PRODUCER = "bank-service";

    @Override
    public void publishUserEvent(DomainEvent event) {
        record(userTopic, event);
    }

    @Override
    public void publishAccountEvent(DomainEvent event) {
        record(accountTopic, event);
    }

    @Override
    public void publish(DomainEvent event) {
        record(transactionTopic, event);
    }

    private void record(String topic, DomainEvent event) {
        try {
            String eventType = resolveEventType(event != null ? event.getEventType() : null);
            Object eventPayload = resolvePayload(event, eventType);
            EventEnvelope<Object> envelope = new EventEnvelope<>(
                    event != null ? event.getEventId() : null,
                    eventType,
                    event != null ? event.getEventTime() : Instant.now(),
                    event != null ? event.getCorrelationId() : null,
                    PRODUCER,
                    eventPayload
            );
            String payload = objectMapper.writeValueAsString(envelope);
            String key = resolveKey(event);
            outboxRepository.save(OutboxEventEntity.newEvent(topic, key, envelope.eventType(), payload));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event for outbox topic " + topic, e);
        }
    }

    private static String resolveKey(DomainEvent event) {
        UUID entityId = event != null ? event.getEntityId() : null;
        if (entityId != null) {
            return entityId.toString();
        }
        UUID eventId = event != null ? event.getEventId() : null;
        return eventId != null ? eventId.toString() : null;
    }

    private static String resolveEventType(EventType eventType) {
        if (eventType == null) {
            return EventTypes.UNKNOWN;
        }
        return switch (eventType) {
            case USER_CREATED -> EventTypes.USER_CREATED;
            case FRIEND_ADDED -> EventTypes.FRIEND_ADDED;
            case FRIEND_REMOVED -> EventTypes.FRIEND_REMOVED;
            case ACCOUNT_CREATED -> EventTypes.ACCOUNT_CREATED;
            case ACCOUNT_DEPOSIT -> EventTypes.ACCOUNT_DEPOSIT;
            case ACCOUNT_WITHDRAWAL -> EventTypes.ACCOUNT_WITHDRAWAL;
            case ACCOUNT_TRANSFER -> EventTypes.ACCOUNT_TRANSFER;
            case TRANSACTION_CREATED -> EventTypes.TRANSACTION_CREATED;
            case UNKNOWN -> EventTypes.UNKNOWN;
        };
    }

    private Object resolvePayload(DomainEvent event, String eventType) {
        if (event == null) {
            return null;
        }
        return switch (eventType) {
            case EventTypes.USER_CREATED -> toUserCreatedPayload(event);
            case EventTypes.FRIEND_ADDED -> toFriendAddedPayload(event);
            case EventTypes.FRIEND_REMOVED -> toFriendRemovedPayload(event);
            case EventTypes.ACCOUNT_CREATED -> toAccountCreatedPayload(event);
            case EventTypes.ACCOUNT_DEPOSIT -> toAccountDepositedPayload(event);
            case EventTypes.ACCOUNT_WITHDRAWAL -> toAccountWithdrawnPayload(event);
            case EventTypes.ACCOUNT_TRANSFER -> toAccountTransferredPayload(event);
            case EventTypes.TRANSACTION_CREATED -> toTransactionCreatedPayload(event);
            default -> new UnknownEventPayload(event.getEntityId(), event.getEventDescription());
        };
    }

    private static UserCreatedPayload toUserCreatedPayload(DomainEvent event) {
        UserCreatedEventData data = requirePayload(event, UserCreatedEventData.class);
        return new UserCreatedPayload(
                data.userId(),
                data.login(),
                data.name(),
                data.age(),
                toContractSex(data.sex()),
                toContractHairColor(data.hairColor()),
                data.description()
        );
    }

    private static FriendAddedPayload toFriendAddedPayload(DomainEvent event) {
        FriendAddedEventData data = requirePayload(event, FriendAddedEventData.class);
        return new FriendAddedPayload(
                data.userId(),
                data.friendId(),
                data.description()
        );
    }

    private static FriendRemovedPayload toFriendRemovedPayload(DomainEvent event) {
        FriendRemovedEventData data = requirePayload(event, FriendRemovedEventData.class);
        return new FriendRemovedPayload(
                data.userId(),
                data.friendId(),
                data.description()
        );
    }

    private static AccountCreatedPayload toAccountCreatedPayload(DomainEvent event) {
        AccountCreatedEventData data = requirePayload(event, AccountCreatedEventData.class);
        return new AccountCreatedPayload(
                data.accountId(),
                data.ownerId(),
                data.ownerLogin(),
                data.description()
        );
    }

    private static AccountDepositedPayload toAccountDepositedPayload(DomainEvent event) {
        AccountDepositedEventData data = requirePayload(event, AccountDepositedEventData.class);
        return new AccountDepositedPayload(
                data.accountId(),
                data.amount(),
                data.description()
        );
    }

    private static AccountWithdrawnPayload toAccountWithdrawnPayload(DomainEvent event) {
        AccountWithdrawnEventData data = requirePayload(event, AccountWithdrawnEventData.class);
        return new AccountWithdrawnPayload(
                data.accountId(),
                data.amount(),
                data.description()
        );
    }

    private static AccountTransferredPayload toAccountTransferredPayload(DomainEvent event) {
        AccountTransferredEventData data = requirePayload(event, AccountTransferredEventData.class);
        return new AccountTransferredPayload(
                data.accountId(),
                data.counterpartyAccountId(),
                data.amount(),
                toContractDirection(data.direction()),
                data.description()
        );
    }

    private static TransactionCreatedPayload toTransactionCreatedPayload(DomainEvent event) {
        TransactionCreatedEventData data = requirePayload(event, TransactionCreatedEventData.class);
        return new TransactionCreatedPayload(
                data.transactionId(),
                data.accountId(),
                toContractType(data.transactionType()),
                data.amount(),
                data.createdAt()
        );
    }

    private static <T extends DomainEventData> T requirePayload(DomainEvent event, Class<T> type) {
        DomainEventData payload = event.getPayload();
        if (payload == null) {
            throw new IllegalStateException("Missing payload for event " + event.getEventType());
        }
        if (!type.isInstance(payload)) {
            throw new IllegalStateException("Unexpected payload type " + payload.getClass().getSimpleName()
                    + " for event " + event.getEventType());
        }
        return type.cast(payload);
    }

    private static TransferDirection toContractDirection(org.nikitakapustkin.domain.events.payload.TransferDirection direction) {
        if (direction == null) {
            return null;
        }
        return TransferDirection.valueOf(direction.name());
    }

    private static Sex toContractSex(org.nikitakapustkin.domain.enums.Sex sex) {
        if (sex == null) {
            return null;
        }
        return Sex.valueOf(sex.name());
    }

    private static HairColor toContractHairColor(org.nikitakapustkin.domain.enums.HairColor hairColor) {
        if (hairColor == null) {
            return null;
        }
        return HairColor.valueOf(hairColor.name());
    }

    private static TransactionType toContractType(org.nikitakapustkin.domain.enums.TransactionType type) {
        if (type == null) {
            return null;
        }
        return TransactionType.valueOf(type.name());
    }
}
