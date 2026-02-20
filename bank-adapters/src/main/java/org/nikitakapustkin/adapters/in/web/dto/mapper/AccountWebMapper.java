package org.nikitakapustkin.adapters.in.web.dto.mapper;

import java.math.BigDecimal;
import java.util.UUID;
import org.nikitakapustkin.application.ports.in.commands.CreateAccountCommand;
import org.nikitakapustkin.application.ports.in.commands.DepositMoneyCommand;
import org.nikitakapustkin.application.ports.in.commands.TransferMoneyCommand;
import org.nikitakapustkin.application.ports.in.commands.WithdrawMoneyCommand;
import org.nikitakapustkin.bank.contracts.dto.request.CreateAccountRequestDto;
import org.nikitakapustkin.bank.contracts.dto.request.TransferRequestDto;
import org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto;
import org.nikitakapustkin.domain.models.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountWebMapper {

  public CreateAccountCommand toCreateCommand(CreateAccountRequestDto dto) {
    return new CreateAccountCommand(dto.ownerId());
  }

  public TransferMoneyCommand toTransferCommand(UUID fromAccountId, TransferRequestDto dto) {
    return new TransferMoneyCommand(fromAccountId, dto.toAccountId(), dto.amount());
  }

  public AccountResponseDto toResponse(Account account) {
    return new AccountResponseDto(account.getId(), account.getUserId(), account.getBalance());
  }

  public DepositMoneyCommand toDepositCommand(UUID accountId, BigDecimal amount) {
    return new DepositMoneyCommand(accountId, amount);
  }

  public WithdrawMoneyCommand toWithdrawCommand(UUID accountId, BigDecimal amount) {
    return new WithdrawMoneyCommand(accountId, amount);
  }
}
