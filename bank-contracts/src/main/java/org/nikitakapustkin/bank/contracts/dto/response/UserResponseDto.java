package org.nikitakapustkin.bank.contracts.dto.response;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.bank.contracts.enums.HairColor;
import org.nikitakapustkin.bank.contracts.enums.Sex;

public record UserResponseDto(
    UUID id,
    String login,
    String name,
    int age,
    Sex sex,
    HairColor hairColor,
    List<String> friendsLogins) {}
