package org.nikitakapustkin.bank.contracts.dto.response;

import java.util.List;

public record UserDetailsResponseDto(
        UserResponseDto user,
        List<String> friendsLogins,
        List<AccountResponseDto> accounts
) {}
