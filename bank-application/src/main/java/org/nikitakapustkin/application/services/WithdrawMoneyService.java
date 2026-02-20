package org.nikitakapustkin.application.services;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.WithdrawMoneyUseCase;
import org.nikitakapustkin.application.ports.in.commands.WithdrawMoneyCommand;
import org.nikitakapustkin.application.ports.out.LoadAccountPort;
import org.nikitakapustkin.application.ports.out.PublishAccountEventPort;
import org.nikitakapustkin.application.ports.out.PublishTransactionEventPort;
import org.nikitakapustkin.application.ports.out.RecordTransactionPort;
import org.nikitakapustkin.application.ports.out.UpdateAccountStatePort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.enums.TransactionType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.AccountWithdrawnEventData;
import org.nikitakapustkin.domain.events.payload.TransactionCreatedEventData;
import org.nikitakapustkin.domain.exceptions.AccountNotFoundException;
import org.nikitakapustkin.domain.models.Transaction;

@RequiredArgsConstructor
public class WithdrawMoneyService implements WithdrawMoneyUseCase {

  private final LoadAccountPort loadAccountPort;
  private final UpdateAccountStatePort updateAccountStatePort;
  private final RecordTransactionPort recordTransactionPort;
  private final PublishAccountEventPort publishAccountEventPort;
  private final PublishTransactionEventPort publishTransactionEventPort;

  @Override
  public void withdraw(WithdrawMoneyCommand cmd) {
    UUID correlationId = UUID.randomUUID();
    var account =
        loadAccountPort
            .loadAccount(cmd.getAccountId())
            .orElseThrow(
                () -> new AccountNotFoundException("Account not found: " + cmd.getAccountId()));

    account.withdraw(cmd.getAmount());
    updateAccountStatePort.updateAccount(account);

    Transaction saved =
        recordTransactionPort.recordTransaction(
            Transaction.builder()
                .accountId(account.getId())
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(cmd.getAmount().negate())
                .createdAt(Instant.now())
                .build());

    String txDescription = "Transaction created for account " + saved.getAccountId();
    publishTransactionEventPort.publish(
        new DomainEvent(
            saved.getId(),
            correlationId,
            saved.getAccountId(),
            EventType.TRANSACTION_CREATED,
            saved.getCreatedAt(),
            txDescription,
            new TransactionCreatedEventData(
                saved.getId(),
                saved.getAccountId(),
                saved.getTransactionType(),
                saved.getAmount(),
                saved.getCreatedAt())));

    String description =
        "Withdraw " + cmd.getAmount().toPlainString() + " from account " + account.getId();
    publishAccountEventPort.publishAccountEvent(
        DomainEvent.now(
            account.getId(),
            EventType.ACCOUNT_WITHDRAWAL,
            description,
            correlationId,
            new AccountWithdrawnEventData(account.getId(), cmd.getAmount(), description)));
  }
}
