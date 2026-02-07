package org.nikitakapustkin.application.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nikitakapustkin.application.ports.in.commands.TransferMoneyCommand;
import org.nikitakapustkin.application.ports.out.LoadAccountPort;
import org.nikitakapustkin.application.ports.out.LoadFriendsPort;
import org.nikitakapustkin.application.ports.out.PublishAccountEventPort;
import org.nikitakapustkin.application.ports.out.PublishTransactionEventPort;
import org.nikitakapustkin.application.ports.out.RecordTransactionPort;
import org.nikitakapustkin.application.ports.out.UpdateAccountStatePort;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.enums.TransactionType;
import org.nikitakapustkin.domain.events.payload.AccountTransferredEventData;
import org.nikitakapustkin.domain.events.payload.TransferDirection;
import org.nikitakapustkin.domain.events.payload.TransactionCreatedEventData;
import org.nikitakapustkin.domain.exceptions.AccountNotFoundException;
import org.nikitakapustkin.domain.models.Account;
import org.nikitakapustkin.domain.models.Transaction;
import org.nikitakapustkin.domain.services.CommissionPolicy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferMoneyServiceTest {

    @Mock LoadAccountPort loadAccountPort;
    @Mock UpdateAccountStatePort updateAccountStatePort;
    @Mock LoadFriendsPort loadFriendsPort;
    @Mock RecordTransactionPort recordTransactionPort;
    @Mock PublishAccountEventPort publishAccountEventPort;
    @Mock PublishTransactionEventPort publishTransactionEventPort;
    @Mock CommissionPolicy commissionPolicy;

    @InjectMocks TransferMoneyService service;

    @Captor ArgumentCaptor<Transaction> txCaptor;
    @Captor ArgumentCaptor<DomainEvent> eventCaptor;
    @Captor ArgumentCaptor<DomainEvent> txEventCaptor;

    @Test
    void transfer_between_friends_applies_3_percent_commission_and_records_two_transactions() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        UUID fromUserId = UUID.randomUUID();
        UUID toUserId = UUID.randomUUID();

        Account from = Account.builder()
                .id(fromId)
                .userId(fromUserId)
                .balance(new BigDecimal("100.00"))
                .build();

        Account to = Account.builder()
                .id(toId)
                .userId(toUserId)
                .balance(new BigDecimal("0.00"))
                .build();

        when(loadAccountPort.loadAccount(fromId)).thenReturn(Optional.of(from));
        when(loadAccountPort.loadAccount(toId)).thenReturn(Optional.of(to));
        when(loadFriendsPort.loadFriendsIds(fromUserId)).thenReturn(List.of(toUserId));
        when(commissionPolicy.rateFor(fromUserId, toUserId, true)).thenReturn(new BigDecimal("0.03"));
        when(recordTransactionPort.recordTransaction(any())).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        service.transferMoney(new TransferMoneyCommand(fromId, toId, new BigDecimal("100.00")));

        assertThat(from.getBalance()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(to.getBalance()).isEqualByComparingTo(new BigDecimal("97.00"));

        verify(updateAccountStatePort).updateAccount(from);
        verify(updateAccountStatePort).updateAccount(to);

        verify(recordTransactionPort, times(2)).recordTransaction(txCaptor.capture());
        verify(publishAccountEventPort, times(2)).publishAccountEvent(eventCaptor.capture());
        verify(publishTransactionEventPort, times(2)).publish(txEventCaptor.capture());
        List<Transaction> txs = txCaptor.getAllValues();
        List<DomainEvent> events = eventCaptor.getAllValues();
        List<DomainEvent> txEvents = txEventCaptor.getAllValues();

        assertThat(txs).hasSize(2);
        assertThat(txs).allSatisfy(tx -> {
            assertThat(tx.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
            assertThat(tx.getCreatedAt()).isNotNull();
        });

        Transaction fromTx = txs.stream().filter(t -> fromId.equals(t.getAccountId())).findFirst().orElseThrow();
        Transaction toTx = txs.stream().filter(t -> toId.equals(t.getAccountId())).findFirst().orElseThrow();

        assertThat(fromTx.getAmount()).isEqualByComparingTo(new BigDecimal("-100.00"));
        assertThat(toTx.getAmount()).isEqualByComparingTo(new BigDecimal("97.00"));

        assertThat(events).hasSize(2);
        DomainEvent outEvent = events.stream().filter(e -> fromId.equals(e.getEntityId())).findFirst().orElseThrow();
        DomainEvent inEvent = events.stream().filter(e -> toId.equals(e.getEntityId())).findFirst().orElseThrow();

        assertThat(outEvent.getEventType()).isEqualTo(EventType.ACCOUNT_TRANSFER);
        assertThat(inEvent.getEventType()).isEqualTo(EventType.ACCOUNT_TRANSFER);
        assertThat(outEvent.getCorrelationId()).isNotNull();
        assertThat(outEvent.getCorrelationId()).isEqualTo(inEvent.getCorrelationId());
        UUID correlationId = outEvent.getCorrelationId();
        assertThat(outEvent.getEventTime()).isNotNull();
        assertThat(outEvent.getEventTime()).isEqualTo(inEvent.getEventTime());
        assertThat(outEvent.getEventDescription()).contains("Transfer out").contains(toId.toString());
        assertThat(inEvent.getEventDescription()).contains("Transfer in").contains(fromId.toString());
        assertThat(outEvent.getPayload()).isInstanceOf(AccountTransferredEventData.class);
        assertThat(inEvent.getPayload()).isInstanceOf(AccountTransferredEventData.class);
        AccountTransferredEventData outPayload = (AccountTransferredEventData) outEvent.getPayload();
        AccountTransferredEventData inPayload = (AccountTransferredEventData) inEvent.getPayload();
        assertThat(outPayload.accountId()).isEqualTo(fromId);
        assertThat(outPayload.counterpartyAccountId()).isEqualTo(toId);
        assertThat(outPayload.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(outPayload.direction()).isEqualTo(TransferDirection.OUT);
        assertThat(inPayload.accountId()).isEqualTo(toId);
        assertThat(inPayload.counterpartyAccountId()).isEqualTo(fromId);
        assertThat(inPayload.amount()).isEqualByComparingTo(new BigDecimal("97.00"));
        assertThat(inPayload.direction()).isEqualTo(TransferDirection.IN);

        assertThat(txEvents).hasSize(2);
        DomainEvent outTxEvent = txEvents.stream().filter(e -> fromId.equals(e.getEntityId())).findFirst().orElseThrow();
        DomainEvent inTxEvent = txEvents.stream().filter(e -> toId.equals(e.getEntityId())).findFirst().orElseThrow();
        assertThat(outTxEvent.getEventType()).isEqualTo(EventType.TRANSACTION_CREATED);
        assertThat(inTxEvent.getEventType()).isEqualTo(EventType.TRANSACTION_CREATED);
        assertThat(outTxEvent.getEventId()).isNotNull();
        assertThat(inTxEvent.getEventId()).isNotNull();
        assertThat(outTxEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(inTxEvent.getCorrelationId()).isEqualTo(correlationId);
        assertThat(outTxEvent.getPayload()).isInstanceOf(TransactionCreatedEventData.class);
        assertThat(inTxEvent.getPayload()).isInstanceOf(TransactionCreatedEventData.class);
        TransactionCreatedEventData outTxPayload = (TransactionCreatedEventData) outTxEvent.getPayload();
        TransactionCreatedEventData inTxPayload = (TransactionCreatedEventData) inTxEvent.getPayload();
        assertThat(outTxPayload.transactionId()).isEqualTo(outTxEvent.getEventId());
        assertThat(inTxPayload.transactionId()).isEqualTo(inTxEvent.getEventId());
        assertThat(outTxPayload.accountId()).isEqualTo(fromId);
        assertThat(inTxPayload.accountId()).isEqualTo(toId);
        assertThat(outTxPayload.transactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(inTxPayload.transactionType()).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    void transfer_between_non_friends_applies_10_percent_commission() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        UUID fromUserId = UUID.randomUUID();
        UUID toUserId = UUID.randomUUID();

        Account from = Account.builder()
                .id(fromId)
                .userId(fromUserId)
                .balance(new BigDecimal("50.00"))
                .build();

        Account to = Account.builder()
                .id(toId)
                .userId(toUserId)
                .balance(new BigDecimal("0.00"))
                .build();

        when(loadAccountPort.loadAccount(fromId)).thenReturn(Optional.of(from));
        when(loadAccountPort.loadAccount(toId)).thenReturn(Optional.of(to));
        when(loadFriendsPort.loadFriendsIds(fromUserId)).thenReturn(List.of());
        when(commissionPolicy.rateFor(fromUserId, toUserId, false)).thenReturn(new BigDecimal("0.10"));
        when(recordTransactionPort.recordTransaction(any())).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        service.transferMoney(new TransferMoneyCommand(fromId, toId, new BigDecimal("50.00")));

        assertThat(from.getBalance()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(to.getBalance()).isEqualByComparingTo(new BigDecimal("45.00"));
        verify(publishAccountEventPort, times(2)).publishAccountEvent(any());
        verify(publishTransactionEventPort, times(2)).publish(any());
    }

    @Test
    void transfer_throws_when_from_account_not_found() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        when(loadAccountPort.loadAccount(fromId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transferMoney(new TransferMoneyCommand(fromId, toId, new BigDecimal("10.00"))))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(fromId.toString());

        verifyNoInteractions(updateAccountStatePort);
        verifyNoInteractions(recordTransactionPort);
        verifyNoInteractions(publishAccountEventPort);
        verifyNoInteractions(publishTransactionEventPort);
    }
}
