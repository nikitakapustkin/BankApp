package org.nikitakapustkin.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.adapters.out.persistence.jpa.AccountJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.jpa.UserJpaRepository;
import org.nikitakapustkin.adapters.out.persistence.mapper.AccountMapper;
import org.nikitakapustkin.application.ports.out.CreateAccountPort;
import org.nikitakapustkin.application.ports.out.LoadAccountPort;
import org.nikitakapustkin.application.ports.out.LoadAccountsPort;
import org.nikitakapustkin.application.ports.out.UpdateAccountStatePort;
import org.nikitakapustkin.domain.models.Account;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter
    implements LoadAccountPort, LoadAccountsPort, CreateAccountPort, UpdateAccountStatePort {

  private final AccountJpaRepository accounts;
  private final UserJpaRepository users;

  @Override
  @Transactional(readOnly = true)
  public Optional<Account> loadAccount(UUID accountId) {
    return accounts.findById(accountId).map(AccountMapper::toDomain);
  }

  @Override
  @Transactional
  public Account createAccount(Account account) {
    var userRef = users.getReferenceById(account.getUserId());
    var entity = AccountMapper.toJpaEntity(account, userRef);
    return AccountMapper.toDomain(accounts.save(entity));
  }

  @Override
  @Transactional
  public void updateAccount(Account account) {
    var entity =
        accounts
            .findById(account.getId())
            .orElseThrow(() -> new IllegalStateException("Account not found: " + account.getId()));

    AccountMapper.applyToJpaEntity(account, entity);

    AccountMapper.toDomain(accounts.save(entity));
  }

  @Override
  public List<Account> loadAccounts(UUID userId) {
    if (userId == null) {
      return accounts.findAll().stream().map(AccountMapper::toDomain).toList();
    }
    return accounts.findByUser_Id(userId).stream().map(AccountMapper::toDomain).toList();
  }
}
