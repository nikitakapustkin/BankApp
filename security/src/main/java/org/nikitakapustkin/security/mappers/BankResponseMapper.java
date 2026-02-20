package org.nikitakapustkin.security.mappers;

import java.util.List;
import java.util.Objects;
import org.nikitakapustkin.security.dto.AccountResponseDto;
import org.nikitakapustkin.security.dto.TransactionResponseDto;
import org.nikitakapustkin.security.dto.UserResponseDto;

public final class BankResponseMapper {
  private BankResponseMapper() {}

  public static AccountResponseDto toAccountResponseDto(
      org.nikitakapustkin.bank.contracts.dto.response.AccountResponseDto account) {
    if (account == null) {
      return null;
    }
    return new AccountResponseDto(account.id(), account.ownerId(), account.balance(), null);
  }

  public static TransactionResponseDto toTransactionResponseDto(
      org.nikitakapustkin.bank.contracts.dto.response.TransactionResponseDto tx) {
    if (tx == null) {
      return null;
    }
    return new TransactionResponseDto(
        tx.id(), tx.accountId(), tx.type(), tx.amount(), tx.createdAt());
  }

  public static UserResponseDto toUserResponseDto(
      org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto user) {
    if (user == null) {
      return null;
    }
    return new UserResponseDto(
        user.id(),
        user.login(),
        user.name(),
        user.age(),
        user.sex() != null ? user.sex().name() : null,
        user.hairColor() != null ? user.hairColor().name() : null,
        user.friendsLogins(),
        List.of());
  }

  public static UserResponseDto toUserResponseDto(
      org.nikitakapustkin.bank.contracts.dto.response.UserDetailsResponseDto details) {
    if (details == null || details.user() == null) {
      return null;
    }
    org.nikitakapustkin.bank.contracts.dto.response.UserResponseDto user = details.user();
    List<String> friends =
        details.friendsLogins() != null ? details.friendsLogins() : user.friendsLogins();
    List<AccountResponseDto> accounts =
        details.accounts() == null
            ? List.of()
            : details.accounts().stream()
                .filter(Objects::nonNull)
                .map(BankResponseMapper::toAccountResponseDto)
                .toList();

    return new UserResponseDto(
        user.id(),
        user.login(),
        user.name(),
        user.age(),
        user.sex() != null ? user.sex().name() : null,
        user.hairColor() != null ? user.hairColor().name() : null,
        friends,
        accounts);
  }
}
