package org.nikitakapustkin.bank.contracts.dto.response;

import java.util.List;

public record AccountDetailsResponseDto(
    AccountResponseDto account, List<TransactionResponseDto> transactions) {}
