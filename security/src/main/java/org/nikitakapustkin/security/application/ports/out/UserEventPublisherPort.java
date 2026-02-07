package org.nikitakapustkin.security.application.ports.out;

import org.nikitakapustkin.security.dto.UserCreateRequestDto;

import java.util.UUID;

public interface UserEventPublisherPort {
    void enqueueUserCreated(UserCreateRequestDto userCreateDto, UUID userId);
}
