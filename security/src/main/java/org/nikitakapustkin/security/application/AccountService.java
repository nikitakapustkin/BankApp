package org.nikitakapustkin.security.application;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.security.application.ports.out.AccountBankClientPort;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.DepositRequestDto;
import org.nikitakapustkin.security.dto.TransactionResponseDto;
import org.nikitakapustkin.security.dto.TransferRequestDto;
import org.nikitakapustkin.security.dto.WithdrawRequestDto;
import org.nikitakapustkin.security.exceptions.ForbiddenException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class AccountService {
    private final AccountBankClientPort accountBankClient;

    public AccountResponseDto createAccount(UUID userId) {
        return accountBankClient.createAccount(userId);
    }

    public void deposit(UUID accountId, DepositRequestDto depositRequest, UUID userId) {
        getUserAccountById(accountId, userId);
        accountBankClient.deposit(accountId, depositRequest);
    }

    public List<AccountResponseDto> getUserAccounts(UUID userId) {
        return accountBankClient.getUserAccounts(userId);
    }

    public AccountResponseDto getUserAccountById(UUID accountId, UUID userId) {
        List<AccountResponseDto> accounts = getUserAccounts(userId);
        boolean allowed = accounts.stream()
                .anyMatch(account -> accountId.equals(account.getAccountId()));
        if (!allowed) {
            throw new ForbiddenException("Access denied");
        }
        return accountBankClient.getAccountById(accountId);
    }

    public void withdraw(UUID accountId, WithdrawRequestDto withdrawRequest, UUID userId) {
        getUserAccountById(accountId, userId);
        accountBankClient.withdraw(accountId, withdrawRequest);
    }

    public void transfer(UUID fromAccountId, TransferRequestDto transferRequest, UUID userId) {
        getUserAccountById(fromAccountId, userId);
        accountBankClient.transfer(fromAccountId, transferRequest);
    }

    public List<AccountResponseDto> getAllAccounts() {
        return accountBankClient.getAllAccounts();
    }

    public List<AccountResponseDto> getAccountsByUserId(UUID userId) {
        return getUserAccounts(userId);
    }

    public AccountResponseDto getAccountById(UUID accountId) {
        return accountBankClient.getAccountById(accountId);
    }

    public List<TransactionResponseDto> getUserTransactions(UUID userId, String type, UUID accountId) {
        if (accountId != null) {
            getUserAccountById(accountId, userId);
            return accountBankClient.getTransactions(type, accountId);
        }

        return getUserAccounts(userId).stream()
                .map(AccountResponseDto::getAccountId)
                .map(id -> accountBankClient.getTransactions(type, id))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(TransactionResponseDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    public List<TransactionResponseDto> getTransactions(String type, UUID accountId) {
        return accountBankClient.getTransactions(type, accountId);
    }
}
