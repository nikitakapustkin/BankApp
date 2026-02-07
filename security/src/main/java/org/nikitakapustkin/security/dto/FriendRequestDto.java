package org.nikitakapustkin.security.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {
    @NotNull(message = "Friend id is required")
    private UUID friendId;
}
