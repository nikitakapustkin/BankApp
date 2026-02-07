package org.nikitakapustkin.bank.contracts.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FriendRequestDto(
        @NotNull(message = "User id is required")
        UUID userId,

        @NotNull(message = "Friend id is required")
        UUID friendId
) {}
