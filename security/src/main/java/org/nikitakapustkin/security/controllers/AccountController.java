package org.nikitakapustkin.security.controllers;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.AccountService;
import org.nikitakapustkin.security.constants.SecurityApiPaths;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.DepositRequestDto;
import org.nikitakapustkin.security.dto.TransferRequestDto;
import org.nikitakapustkin.security.dto.WithdrawRequestDto;
import org.nikitakapustkin.security.models.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {
  private final AccountService accountService;

  @PostMapping(SecurityApiPaths.USERS_ME_ACCOUNTS)
  public ResponseEntity<AccountResponseDto> createUserAccount(Authentication authentication) {
    UUID userId = requireUserId(authentication);
    AccountResponseDto created = accountService.createAccount(userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PostMapping(SecurityApiPaths.USERS_ME_ACCOUNT_DEPOSIT)
  public ResponseEntity<Void> deposit(
      @PathVariable(name = "accountId") UUID accountId,
      @Valid @RequestBody DepositRequestDto depositRequest,
      Authentication authentication) {
    UUID userId = requireUserId(authentication);
    accountService.deposit(accountId, depositRequest, userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping(SecurityApiPaths.USERS_ME_ACCOUNT_WITHDRAW)
  public ResponseEntity<Void> withdraw(
      @PathVariable(name = "accountId") UUID accountId,
      @Valid @RequestBody WithdrawRequestDto withdrawRequest,
      Authentication authentication) {
    UUID userId = requireUserId(authentication);
    accountService.withdraw(accountId, withdrawRequest, userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping(SecurityApiPaths.USERS_ME_ACCOUNT_TRANSFER)
  public ResponseEntity<Void> transfer(
      @PathVariable(name = "fromAccountId") UUID fromAccountId,
      @Valid @RequestBody TransferRequestDto transferRequest,
      Authentication authentication) {
    UUID userId = requireUserId(authentication);
    accountService.transfer(fromAccountId, transferRequest, userId);
    return ResponseEntity.ok().build();
  }

  @GetMapping(SecurityApiPaths.USERS_ME_ACCOUNTS)
  public ResponseEntity<List<AccountResponseDto>> getUserAccounts(Authentication authentication) {
    UUID userId = requireUserId(authentication);
    return ResponseEntity.ok(accountService.getUserAccounts(userId));
  }

  @GetMapping(SecurityApiPaths.USERS_ME_ACCOUNT_BY_ID)
  public ResponseEntity<AccountResponseDto> getUserAccount(
      @PathVariable("accountId") UUID accountId, Authentication authentication) {
    UUID userId = requireUserId(authentication);
    return ResponseEntity.ok(accountService.getUserAccountById(accountId, userId));
  }

  @GetMapping(SecurityApiPaths.ACCOUNTS)
  public ResponseEntity<List<AccountResponseDto>> getAccounts() {
    return ResponseEntity.ok(accountService.getAllAccounts());
  }

  @GetMapping(SecurityApiPaths.ACCOUNTS_BY_USER)
  public ResponseEntity<List<AccountResponseDto>> getAccountsByUserId(
      @PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
  }

  @GetMapping(SecurityApiPaths.ACCOUNT_BY_ID)
  public ResponseEntity<AccountResponseDto> getAccount(@PathVariable("accountId") UUID accountId) {
    return ResponseEntity.ok(accountService.getAccountById(accountId));
  }

  private static UUID requireUserId(Authentication authentication) {
    if (authentication == null) {
      throw new AccessDeniedException("Access denied");
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof JwtPrincipal jwtPrincipal && jwtPrincipal.userId() != null) {
      return jwtPrincipal.userId();
    }
    throw new AccessDeniedException("Access denied");
  }
}
