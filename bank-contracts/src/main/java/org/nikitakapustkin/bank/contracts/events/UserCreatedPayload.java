package org.nikitakapustkin.bank.contracts.events;

import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;

import java.util.UUID;

public record UserCreatedPayload(
        UUID userId,
        String login,
        String name,
        Integer age,
        Sex sex,
        HairColor hairColor,
        String description
) {}
