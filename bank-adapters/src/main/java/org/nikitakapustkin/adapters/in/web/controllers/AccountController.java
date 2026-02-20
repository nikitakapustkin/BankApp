package org.nikitakapustkin.adapters.in.web.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.adapters.in.web.dto.mapper.AccountWebMapper;
import org.nikitakapustkin.adapters.in.web.dto.mapper.TransactionWebMapper;
import org.nikitakapustkin.adapters.in.web.dto.response.CommonErrorResponses;
import org.nikitakapustkin.application.ports.in.CreateAccountUseCase;
import org.nikitakapustkin.application.ports.in.DepositMoneyUseCase;
import org.nikitakapustkin.application.ports.in.TransferMoneyUseCase;
import org.nikitakapustkin.application.ports.in.WithdrawMoneyUseCase;
import org.nikitakapustkin.application.ports.in.queries.GetAccountQuery;
import org.nikitakapustkin.application.ports.in.queries.GetAccountsQuery;
import org.nikitakapustkin.application.ports.in.queries.GetTransactionsQuery;
import org.nikitakapustkin.bank.contracts.dto.request.CreateAccountRequestDto;
import org.nikitakapustkin.bank.contracts.dto.request.DepositRequestDto;
import org.nikitakapustkin.bank.contracts.dto.request.TransferRequestDto;
import org.nikitakapustkin.bank.contracts.dto.request.WithdrawRequestDto;
import org.nikitakapustkin.bank.contracts.dto.response.AccountDetailsResponseDto;
import org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CommonErrorResponses
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Controller", description = "API for managing user accounts and transactions")
public class AccountController {

  private final GetAccountsQuery getAccountsQuery;
  private final GetAccountQuery getAccountQuery;
  private final GetTransactionsQuery getTransactionsQuery;

  private final CreateAccountUseCase createAccountUseCase;
  private final DepositMoneyUseCase depositMoneyUseCase;
  private final WithdrawMoneyUseCase withdrawMoneyUseCase;
  private final TransferMoneyUseCase transferMoneyUseCase;

  private final AccountWebMapper accountMapper;
  private final TransactionWebMapper transactionMapper;

  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<List<AccountResponseDto>> getAccounts(
      @RequestParam(required = false, name = "userId") UUID userId) {
    var accounts = getAccountsQuery.getAccounts(userId);
    return ResponseEntity.ok(accounts.stream().map(accountMapper::toResponse).toList());
  }

  @PostMapping
  @Transactional
  public ResponseEntity<AccountResponseDto> createAccount(
      @RequestBody @Valid CreateAccountRequestDto dto) {
    var created = createAccountUseCase.createAccount(accountMapper.toCreateCommand(dto));
    return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(created));
  }

  @GetMapping("/{accountId}")
  @Transactional(readOnly = true)
  public ResponseEntity<AccountDetailsResponseDto> getAccount(
      @PathVariable("accountId") UUID accountId) {
    var account = getAccountQuery.getAccount(accountId);
    var transactions = getTransactionsQuery.getTransactions(null, accountId);

    var accountResponse = accountMapper.toResponse(account);
    var transactionResponses = transactions.stream().map(transactionMapper::toResponse).toList();

    var details = new AccountDetailsResponseDto(accountResponse, transactionResponses);
    return ResponseEntity.ok(details);
  }

  @GetMapping("/{accountId}/balance")
  @Transactional(readOnly = true)
  public ResponseEntity<BigDecimal> getBalance(
      @Parameter(
              description = "ID of the account",
              example = "123e4567-e89b-12d3-a456-426614174000")
          @PathVariable("accountId")
          UUID accountId) {
    var account = getAccountQuery.getAccount(accountId);
    return ResponseEntity.ok(account.getBalance());
  }

  @PostMapping("/{accountId}/deposit")
  @Transactional
  public ResponseEntity<Void> deposit(
      @PathVariable("accountId") UUID accountId, @Valid @RequestBody DepositRequestDto request) {
    var command = accountMapper.toDepositCommand(accountId, request.amount());
    depositMoneyUseCase.deposit(command);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{accountId}/withdraw")
  @Transactional
  public ResponseEntity<Void> withdraw(
      @PathVariable("accountId") UUID accountId, @Valid @RequestBody WithdrawRequestDto request) {
    var command = accountMapper.toWithdrawCommand(accountId, request.amount());
    withdrawMoneyUseCase.withdraw(command);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{fromAccountId}/transfer")
  @Transactional
  public ResponseEntity<Void> transfer(
      @PathVariable("fromAccountId") UUID fromAccountId,
      @Valid @RequestBody TransferRequestDto request) {
    var command = accountMapper.toTransferCommand(fromAccountId, request);
    transferMoneyUseCase.transferMoney(command);
    return ResponseEntity.ok().build();
  }
}
