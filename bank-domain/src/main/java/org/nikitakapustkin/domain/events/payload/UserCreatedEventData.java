package org.nikitakapustkin.domain.events.payload;

import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;

import java.util.UUID;

public record UserCreatedEventData(
        UUID userId,
        String login,
        String name,
        Integer age,
        Sex sex,
        HairColor hairColor,
        String description
) implements DomainEventData {}
