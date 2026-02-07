package org.nikitakapustkin.security.application.ports.out;

import org.nikitakapustkin.security.dto.StorageEventResponseDto;

import java.util.List;
import java.util.UUID;

public interface StorageEventClientPort {
    List<StorageEventResponseDto> getEvents(
            String source,
            String eventType,
            UUID entityId,
            UUID correlationId,
            String transactionType,
            Integer limit
    );
}
