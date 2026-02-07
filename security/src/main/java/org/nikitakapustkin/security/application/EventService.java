package org.nikitakapustkin.security.application;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.ports.out.StorageEventClientPort;
import org.nikitakapustkin.security.dto.StorageEventResponseDto;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class EventService {
    private final StorageEventClientPort storageEventClient;

    public List<StorageEventResponseDto> getEvents(
            String source,
            String eventType,
            UUID entityId,
            UUID correlationId,
            String transactionType,
            Integer limit
    ) {
        return storageEventClient.getEvents(source, eventType, entityId, correlationId, transactionType, limit);
    }
}
