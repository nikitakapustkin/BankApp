package org.nikitakapustkin.storage.application;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.bank.contracts.enums.TransactionType;
import org.nikitakapustkin.storage.adapters.out.persistence.AccountEventRepository;
import org.nikitakapustkin.storage.adapters.out.persistence.TransactionEventRepository;
import org.nikitakapustkin.storage.adapters.out.persistence.UserEventRepository;
import org.nikitakapustkin.storage.dto.StorageEventResponseDto;
import org.nikitakapustkin.storage.events.AccountEvent;
import org.nikitakapustkin.storage.events.TransactionEvent;
import org.nikitakapustkin.storage.events.UserEvent;
import org.springframework.data.domain.PageRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
public class EventQueryService {
    private final UserEventRepository userEventRepository;
    private final AccountEventRepository accountEventRepository;
    private final TransactionEventRepository transactionEventRepository;

    public List<StorageEventResponseDto> getEvents(
            String source,
            String eventType,
            UUID entityId,
            UUID correlationId,
            String transactionType,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        String normalizedSource = source == null ? "ALL" : source.toUpperCase(Locale.ROOT);
        String normalizedEventType = normalizeEventType(eventType);
        TransactionType transactionTypeEnum = parseTransactionType(transactionType);
        PageRequest page = PageRequest.of(0, safeLimit);

        List<StorageEventResponseDto> events = switch (normalizedSource) {
            case "USER" -> fetchUserEvents(normalizedEventType, entityId, correlationId, page);
            case "ACCOUNT" -> fetchAccountEvents(normalizedEventType, entityId, correlationId, page);
            case "TRANSACTION" -> fetchTransactionEvents(normalizedEventType, entityId, correlationId, transactionTypeEnum, page);
            default -> mergeAllSources(normalizedEventType, entityId, correlationId, transactionTypeEnum, page, safeLimit);
        };

        return events.stream()
                .sorted(Comparator.comparing(
                                StorageEventResponseDto::getEventTime,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .reversed())
                .limit(safeLimit)
                .toList();
    }

    private List<StorageEventResponseDto> fetchUserEvents(
            String eventType,
            UUID userId,
            UUID correlationId,
            PageRequest page
    ) {
        return userEventRepository.findEvents(eventType, userId, correlationId, page).stream()
                .map(this::toUserDto)
                .toList();
    }

    private List<StorageEventResponseDto> fetchAccountEvents(
            String eventType,
            UUID accountId,
            UUID correlationId,
            PageRequest page
    ) {
        return accountEventRepository.findEvents(eventType, accountId, correlationId, page).stream()
                .map(this::toAccountDto)
                .toList();
    }

    private List<StorageEventResponseDto> fetchTransactionEvents(
            String eventType,
            UUID accountId,
            UUID correlationId,
            TransactionType transactionType,
            PageRequest page
    ) {
        return transactionEventRepository.findEvents(eventType, accountId, correlationId, transactionType, page).stream()
                .map(this::toTransactionDto)
                .toList();
    }

    private List<StorageEventResponseDto> mergeAllSources(
            String eventType,
            UUID entityId,
            UUID correlationId,
            TransactionType transactionType,
            PageRequest page,
            int limit
    ) {
        var user = fetchUserEvents(eventType, entityId, correlationId, page);
        var account = fetchAccountEvents(eventType, entityId, correlationId, page);
        var transaction = fetchTransactionEvents(eventType, entityId, correlationId, transactionType, page);

        return java.util.stream.Stream.of(user, account, transaction)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(
                                StorageEventResponseDto::getEventTime,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .reversed())
                .limit(limit)
                .toList();
    }

    private StorageEventResponseDto toUserDto(UserEvent event) {
        return new StorageEventResponseDto(
                event.getEventId(),
                event.getCorrelationId(),
                "USER",
                event.getUserId(),
                null,
                null,
                null,
                null,
                event.getEventType(),
                event.getEventTime(),
                event.getEventDescription(),
                event.getPayloadType(),
                event.getPayload()
        );
    }

    private StorageEventResponseDto toAccountDto(AccountEvent event) {
        return new StorageEventResponseDto(
                event.getEventId(),
                event.getCorrelationId(),
                "ACCOUNT",
                event.getAccountId(),
                null,
                null,
                null,
                null,
                event.getEventType(),
                event.getEventTime(),
                event.getEventDescription(),
                event.getPayloadType(),
                event.getPayload()
        );
    }

    private StorageEventResponseDto toTransactionDto(TransactionEvent event) {
        return new StorageEventResponseDto(
                event.getEventId(),
                event.getCorrelationId(),
                "TRANSACTION",
                event.getAccountId(),
                event.getTransactionId(),
                event.getTransactionType() == null ? null : event.getTransactionType().name(),
                event.getAmount(),
                event.getCreatedAt(),
                event.getEventType(),
                event.getEventTime(),
                event.getEventDescription(),
                event.getPayloadType(),
                event.getPayload()
        );
    }

    private static TransactionType parseTransactionType(String transactionType) {
        if (transactionType == null || transactionType.isBlank()) {
            return null;
        }
        return TransactionType.valueOf(transactionType.toUpperCase(Locale.ROOT));
    }

    private static String normalizeEventType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return null;
        }
        return eventType.toLowerCase(Locale.ROOT);
    }
}
