package org.nikitakapustkin.bank.contracts.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponseDto(UUID id, UUID ownerId, BigDecimal balance) {}
