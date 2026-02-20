package org.nikitakapustkin.domain.events.payload;

import java.util.UUID;
import org.nikitakapustkin.domain.enums.HairColor;
import org.nikitakapustkin.domain.enums.Sex;

public record UserCreatedEventData(
    UUID userId,
    String login,
    String name,
    Integer age,
    Sex sex,
    HairColor hairColor,
    String description)
    implements DomainEventData {}
