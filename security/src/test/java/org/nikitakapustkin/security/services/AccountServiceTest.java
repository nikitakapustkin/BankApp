package org.nikitakapustkin.security.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.security.application.AccountService;
import org.nikitakapustkin.security.application.ports.out.AccountBankClientPort;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.DepositRequestDto;
import org.nikitakapustkin.security.exceptions.ForbiddenException;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  @Mock AccountBankClientPort accountWebClient;

  @InjectMocks AccountService service;

  @Test
  void create_account_calls_web_client() {
    UUID userId = UUID.randomUUID();
    AccountResponseDto created =
        new AccountResponseDto(UUID.randomUUID(), userId, BigDecimal.ZERO, null);
    when(accountWebClient.createAccount(userId)).thenReturn(created);

    AccountResponseDto result = service.createAccount(userId);

    assertThat(result).isEqualTo(created);
    verify(accountWebClient).createAccount(userId);
  }

  @Test
  void get_user_account_by_id_returns_details() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    AccountResponseDto listed = new AccountResponseDto(accountId, null, null, null);
    when(accountWebClient.getUserAccounts(userId)).thenReturn(List.of(listed));

    AccountResponseDto details = new AccountResponseDto(accountId, userId, null, null);
    when(accountWebClient.getAccountById(accountId)).thenReturn(details);

    AccountResponseDto result = service.getUserAccountById(accountId, userId);

    assertThat(result).isEqualTo(details);
    verify(accountWebClient).getAccountById(accountId);
  }

  @Test
  void get_user_account_by_id_denies_access_when_not_owned() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(accountWebClient.getUserAccounts(userId)).thenReturn(List.of());

    assertThatThrownBy(() -> service.getUserAccountById(accountId, userId))
        .isInstanceOf(ForbiddenException.class);

    verify(accountWebClient, never()).getAccountById(any());
  }

  @Test
  void deposit_calls_web_client_when_access_allowed() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();

    AccountResponseDto listed = new AccountResponseDto(accountId, null, null, null);
    when(accountWebClient.getUserAccounts(userId)).thenReturn(List.of(listed));
    when(accountWebClient.getAccountById(accountId)).thenReturn(listed);

    DepositRequestDto request = new DepositRequestDto(new BigDecimal("10.00"));

    service.deposit(accountId, request, userId);

    verify(accountWebClient).deposit(accountId, request);
  }
}
