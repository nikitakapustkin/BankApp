package org.nikitakapustkin.security.application.ports.out;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.security.dto.StorageEventResponseDto;

public interface StorageEventClientPort {
  List<StorageEventResponseDto> getEvents(
      String source,
      String eventType,
      UUID entityId,
      UUID correlationId,
      String transactionType,
      Integer limit);
}
