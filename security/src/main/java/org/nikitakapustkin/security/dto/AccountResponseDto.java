package org.nikitakapustkin.security.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Value;

@Value
public class AccountResponseDto {
  private UUID accountId;

  private UUID ownerId;

  private BigDecimal balance;

  private List<TransactionResponseDto> transactions;
}
