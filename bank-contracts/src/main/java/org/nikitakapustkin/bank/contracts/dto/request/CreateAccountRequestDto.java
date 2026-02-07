package org.nikitakapustkin.bank.contracts.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequestDto(
        @NotNull(message = "Owner id is required")
        UUID ownerId
) {}
