package org.nikitakapustkin.security.application.ports.out;

import java.util.List;
import java.util.UUID;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.DepositRequestDto;
import org.nikitakapustkin.security.dto.TransactionResponseDto;
import org.nikitakapustkin.security.dto.TransferRequestDto;
import org.nikitakapustkin.security.dto.WithdrawRequestDto;

public interface AccountBankClientPort {
  AccountResponseDto createAccount(UUID ownerId);

  List<AccountResponseDto> getUserAccounts(UUID userId);

  void deposit(UUID accountId, DepositRequestDto depositRequest);

  void withdraw(UUID accountId, WithdrawRequestDto withdrawRequest);

  void transfer(UUID fromAccountId, TransferRequestDto transferRequest);

  List<AccountResponseDto> getAllAccounts();

  AccountResponseDto getAccountById(UUID accountId);

  List<TransactionResponseDto> getTransactions(String type, UUID accountId);
}
