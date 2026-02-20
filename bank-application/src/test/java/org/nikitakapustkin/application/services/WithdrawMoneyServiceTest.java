package org.nikitakapustkin.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.nikitakapustkin.domain.exceptions.NotEnoughMoneyException;
import org.nikitakapustkin.domain.models.Account;
import org.nikitakapustkin.domain.models.Transaction;

@ExtendWith(MockitoExtension.class)
class WithdrawMoneyServiceTest {

  @Mock LoadAccountPort loadAccountPort;
  @Mock UpdateAccountStatePort updateAccountStatePort;
  @Mock RecordTransactionPort recordTransactionPort;
  @Mock PublishAccountEventPort publishAccountEventPort;
  @Mock PublishTransactionEventPort publishTransactionEventPort;

  @InjectMocks WithdrawMoneyService service;

  @Captor ArgumentCaptor<Transaction> txCaptor;
  @Captor ArgumentCaptor<DomainEvent> eventCaptor;
  @Captor ArgumentCaptor<DomainEvent> txEventCaptor;

  @Test
  void withdraw_updates_balance_persists_account_and_records_negative_transaction() {
    UUID accountId = UUID.randomUUID();
    Account account =
        Account.builder()
            .id(accountId)
            .ownerLogin("alice")
            .balance(new BigDecimal("50.00"))
            .build();

    when(loadAccountPort.loadAccount(accountId)).thenReturn(Optional.of(account));
    when(recordTransactionPort.recordTransaction(any()))
        .thenAnswer(
            invocation -> {
              Transaction tx = invocation.getArgument(0);
              tx.setId(UUID.randomUUID());
              return tx;
            });

    service.withdraw(new WithdrawMoneyCommand(accountId, new BigDecimal("10.00")));

    assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("40.00"));

    verify(updateAccountStatePort).updateAccount(account);
    verify(recordTransactionPort).recordTransaction(txCaptor.capture());
    verify(publishAccountEventPort).publishAccountEvent(eventCaptor.capture());
    verify(publishTransactionEventPort).publish(txEventCaptor.capture());

    Transaction tx = txCaptor.getValue();
    assertThat(tx.getAccountId()).isEqualTo(accountId);
    assertThat(tx.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
    assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("-10.00"));
    assertThat(tx.getCreatedAt()).isNotNull();

    DomainEvent event = eventCaptor.getValue();
    assertThat(event.getEntityId()).isEqualTo(accountId);
    assertThat(event.getCorrelationId()).isNotNull();
    assertThat(event.getEventType()).isEqualTo(EventType.ACCOUNT_WITHDRAWAL);
    assertThat(event.getEventTime()).isNotNull();
    assertThat(event.getEventDescription()).contains("Withdraw").contains(accountId.toString());

    DomainEvent txEvent = txEventCaptor.getValue();
    assertThat(txEvent.getEventType()).isEqualTo(EventType.TRANSACTION_CREATED);
    assertThat(txEvent.getEventId()).isNotNull();
    assertThat(txEvent.getEntityId()).isEqualTo(accountId);
    assertThat(txEvent.getEventTime()).isNotNull();
    assertThat(txEvent.getCorrelationId()).isEqualTo(event.getCorrelationId());
    assertThat(txEvent.getPayload()).isInstanceOf(TransactionCreatedEventData.class);
    TransactionCreatedEventData txPayload = (TransactionCreatedEventData) txEvent.getPayload();
    assertThat(txPayload.transactionId()).isEqualTo(txEvent.getEventId());
    assertThat(txPayload.accountId()).isEqualTo(accountId);
    assertThat(txPayload.transactionType()).isEqualTo(TransactionType.WITHDRAWAL);
    assertThat(txPayload.amount()).isEqualByComparingTo(new BigDecimal("-10.00"));
    assertThat(txPayload.createdAt()).isEqualTo(txEvent.getEventTime());
    assertThat(event.getPayload()).isInstanceOf(AccountWithdrawnEventData.class);
    AccountWithdrawnEventData payload = (AccountWithdrawnEventData) event.getPayload();
    assertThat(payload.accountId()).isEqualTo(accountId);
    assertThat(payload.amount()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(payload.description()).contains("Withdraw").contains(accountId.toString());
  }

  @Test
  void withdraw_throws_not_enough_money_and_does_not_persist_or_record() {
    UUID accountId = UUID.randomUUID();
    Account account =
        Account.builder().id(accountId).ownerLogin("alice").balance(new BigDecimal("5.00")).build();

    when(loadAccountPort.loadAccount(accountId)).thenReturn(Optional.of(account));

    assertThatThrownBy(
            () -> service.withdraw(new WithdrawMoneyCommand(accountId, new BigDecimal("10.00"))))
        .isInstanceOf(NotEnoughMoneyException.class);

    verifyNoInteractions(updateAccountStatePort);
    verifyNoInteractions(recordTransactionPort);
    verifyNoInteractions(publishAccountEventPort);
    verifyNoInteractions(publishTransactionEventPort);
  }

  @Test
  void withdraw_throws_when_account_not_found() {
    UUID accountId = UUID.randomUUID();
    when(loadAccountPort.loadAccount(accountId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.withdraw(new WithdrawMoneyCommand(accountId, new BigDecimal("1.00"))))
        .isInstanceOf(AccountNotFoundException.class);

    verifyNoInteractions(updateAccountStatePort);
    verifyNoInteractions(recordTransactionPort);
    verifyNoInteractions(publishAccountEventPort);
    verifyNoInteractions(publishTransactionEventPort);
  }
}
