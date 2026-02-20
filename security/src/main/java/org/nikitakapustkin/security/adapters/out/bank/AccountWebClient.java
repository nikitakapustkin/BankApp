package org.nikitakapustkin.security.adapters.out.bank;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.ports.out.AccountBankClientPort;
import org.nikitakapustkin.security.constants.BankApiPaths;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.DepositRequestDto;
import org.nikitakapustkin.security.dto.TransactionResponseDto;
import org.nikitakapustkin.security.dto.TransferRequestDto;
import org.nikitakapustkin.security.dto.WithdrawRequestDto;
import org.nikitakapustkin.security.mappers.BankResponseMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountWebClient implements AccountBankClientPort {
  private final WebClient webClient;
  private final BankWebClientSupport support;

  @Override
  public AccountResponseDto createAccount(UUID ownerId) {
    Mono<org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto> response =
        webClient
            .post()
            .uri(BankApiPaths.ACCOUNTS)
            .bodyValue(
                new org.nikitakapustkin.bank.contracts.dto.request.CreateAccountRequestDto(ownerId))
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto.class);
    org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto created =
        support.maybeRetry(response, false).block();
    return BankResponseMapper.toAccountResponseDto(created);
  }

  @Override
  public List<AccountResponseDto> getUserAccounts(UUID userId) {
    Mono<List<org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto>> response =
        webClient
            .get()
            .uri(BankApiPaths.USER_ACCOUNTS, userId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(new ParameterizedTypeReference<>() {});
    List<org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto> accounts =
        response.retryWhen(support.retrySpec()).block();
    if (accounts == null) {
      return List.of();
    }
    return accounts.stream().map(BankResponseMapper::toAccountResponseDto).toList();
  }

  @Override
  public void deposit(UUID accountId, DepositRequestDto depositRequest) {
    Mono<ResponseEntity<Void>> response =
        webClient
            .post()
            .uri(BankApiPaths.ACCOUNT_DEPOSIT, accountId)
            .bodyValue(
                new org.nikitakapustkin.bank.contracts.dto.request.DepositRequestDto(
                    depositRequest.getAmount()))
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .toEntity(Void.class);
    support.maybeRetry(response, false).block();
  }

  @Override
  public void withdraw(UUID accountId, WithdrawRequestDto withdrawRequest) {
    Mono<ResponseEntity<Void>> response =
        webClient
            .post()
            .uri(BankApiPaths.ACCOUNT_WITHDRAW, accountId)
            .bodyValue(
                new org.nikitakapustkin.bank.contracts.dto.request.WithdrawRequestDto(
                    withdrawRequest.getAmount()))
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .toEntity(Void.class);
    support.maybeRetry(response, false).block();
  }

  @Override
  public void transfer(UUID fromAccountId, TransferRequestDto transferRequest) {
    Mono<ResponseEntity<Void>> response =
        webClient
            .post()
            .uri(BankApiPaths.ACCOUNT_TRANSFER, fromAccountId)
            .bodyValue(
                new org.nikitakapustkin.bank.contracts.dto.request.TransferRequestDto(
                    transferRequest.getToAccountId(), transferRequest.getAmount()))
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .toEntity(Void.class);
    support.maybeRetry(response, false).block();
  }

  @Override
  public List<AccountResponseDto> getAllAccounts() {
    Mono<List<org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto>> response =
        webClient
            .get()
            .uri(BankApiPaths.ACCOUNTS)
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(new ParameterizedTypeReference<>() {});
    List<org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto> accounts =
        response.retryWhen(support.retrySpec()).block();
    if (accounts == null) {
      return List.of();
    }
    return accounts.stream().map(BankResponseMapper::toAccountResponseDto).toList();
  }

  @Override
  public AccountResponseDto getAccountById(UUID accountId) {
    Mono<org.nikitakapustkin.bank.contracts.dto.response.AccountDetailsResponseDto> response =
        webClient
            .get()
            .uri(BankApiPaths.ACCOUNT_BY_ID, accountId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(
                org.nikitakapustkin.bank.contracts.dto.response.AccountDetailsResponseDto.class);
    org.nikitakapustkin.bank.contracts.dto.response.AccountDetailsResponseDto details =
        response.retryWhen(support.retrySpec()).block();
    if (details == null || details.account() == null) {
      return null;
    }
    AccountResponseDto dto = BankResponseMapper.toAccountResponseDto(details.account());
    List<TransactionResponseDto> transactions =
        details.transactions() == null
            ? List.of()
            : details.transactions().stream()
                .filter(Objects::nonNull)
                .map(BankResponseMapper::toTransactionResponseDto)
                .toList();
    if (dto == null) {
      return null;
    }
    return new AccountResponseDto(
        dto.getAccountId(), dto.getOwnerId(), dto.getBalance(), transactions);
  }

  @Override
  public List<TransactionResponseDto> getTransactions(String type, UUID accountId) {
    Mono<List<org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto>> response =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(BankApiPaths.TRANSACTIONS)
                        .queryParamIfPresent("type", Optional.ofNullable(type))
                        .queryParamIfPresent("accountId", Optional.ofNullable(accountId))
                        .build())
            .retrieve()
            .onStatus(HttpStatusCode::isError, support::toBankException)
            .bodyToMono(new ParameterizedTypeReference<>() {});
    List<org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto> transactions =
        response.retryWhen(support.retrySpec()).block();
    if (transactions == null) {
      return List.of();
    }
    return transactions.stream()
        .filter(Objects::nonNull)
        .map(BankResponseMapper::toTransactionResponseDto)
        .toList();
  }
}
