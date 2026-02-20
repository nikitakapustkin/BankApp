package org.nikitakapustkin.security.application.ports.out;

import java.util.UUID;
import org.nikitakapustkin.security.dto.UserCreateRequestDto;

public interface UserEventPublisherPort {
  void enqueueUserCreated(UserCreateRequestDto userCreateDto, UUID userId);
}
