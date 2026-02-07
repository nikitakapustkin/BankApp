package org.nikitakapustkin.application.services;

import lombok.RequiredArgsConstructor;
import org.nikitakapustkin.application.ports.in.TransferMoneyUseCase;
import org.nikitakapustkin.application.ports.in.commands.TransferMoneyCommand;
import org.nikitakapustkin.application.ports.out.LoadAccountPort;
import org.nikitakapustkin.application.ports.out.LoadFriendsPort;
import org.nikitakapustkin.application.ports.out.PublishAccountEventPort;
import org.nikitakapustkin.application.ports.out.PublishTransactionEventPort;
import org.nikitakapustkin.application.ports.out.RecordTransactionPort;
import org.nikitakapustkin.application.ports.out.UpdateAccountStatePort;
import org.nikitakapustkin.domain.enums.EventType;
import org.nikitakapustkin.domain.enums.TransactionType;
import org.nikitakapustkin.domain.events.DomainEvent;
import org.nikitakapustkin.domain.events.payload.AccountTransferredEventData;
import org.nikitakapustkin.domain.events.payload.TransferDirection;
import org.nikitakapustkin.domain.events.payload.TransactionCreatedEventData;
import org.nikitakapustkin.domain.exceptions.AccountNotFoundException;
import org.nikitakapustkin.domain.models.Account;
import org.nikitakapustkin.domain.models.Transaction;
import org.nikitakapustkin.domain.services.CommissionPolicy;
import org.nikitakapustkin.domain.services.TransferRules;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class TransferMoneyService implements TransferMoneyUseCase {

    private final LoadAccountPort loadAccountPort;
    private final UpdateAccountStatePort updateAccountStatePort;
    private final LoadFriendsPort loadFriendsPort;
    private final RecordTransactionPort recordTransactionPort;
    private final PublishAccountEventPort publishAccountEventPort;
    private final PublishTransactionEventPort publishTransactionEventPort;
    private final CommissionPolicy commissionPolicy;

    @Override
    public void transferMoney(TransferMoneyCommand cmd) {
        UUID correlationId = UUID.randomUUID();

        Account from = loadAccountPort.loadAccount(cmd.getFromAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + cmd.getFromAccountId()));
        Account to = loadAccountPort.loadAccount(cmd.getToAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + cmd.getToAccountId()));

        BigDecimal amount = cmd.getAmount();

        boolean friends = false;
        if (from.getUserId() != null && to.getUserId() != null && !from.getUserId().equals(to.getUserId())) {
            friends = loadFriendsPort.loadFriendsIds(from.getUserId()).contains(to.getUserId());
        }

        TransferRules.TransferCalculation calculation = TransferRules.calculate(
                from.getId(),
                to.getId(),
                from.getUserId(),
                to.getUserId(),
                amount,
                friends,
                commissionPolicy
        );

        BigDecimal credited = calculation.credited();

        from.withdraw(amount);
        to.deposit(credited);

        updateAccountStatePort.updateAccount(from);
        updateAccountStatePort.updateAccount(to);

        Instant now = Instant.now();

        Transaction fromTx = recordTransactionPort.recordTransaction(Transaction.builder()
                .accountId(from.getId())
                .transactionType(TransactionType.TRANSFER)
                .amount(amount.negate())
                .createdAt(now)
                .build());

        Transaction toTx = recordTransactionPort.recordTransaction(Transaction.builder()
                .accountId(to.getId())
                .transactionType(TransactionType.TRANSFER)
                .amount(credited)
                .createdAt(now)
                .build());

        publishTransactionEventPort.publish(new DomainEvent(
                fromTx.getId(),
                correlationId,
                fromTx.getAccountId(),
                EventType.TRANSACTION_CREATED,
                fromTx.getCreatedAt(),
                "Transaction created for account " + fromTx.getAccountId(),
                new TransactionCreatedEventData(
                        fromTx.getId(),
                        fromTx.getAccountId(),
                        fromTx.getTransactionType(),
                        fromTx.getAmount(),
                        fromTx.getCreatedAt()
                )
        ));
        publishTransactionEventPort.publish(new DomainEvent(
                toTx.getId(),
                correlationId,
                toTx.getAccountId(),
                EventType.TRANSACTION_CREATED,
                toTx.getCreatedAt(),
                "Transaction created for account " + toTx.getAccountId(),
                new TransactionCreatedEventData(
                        toTx.getId(),
                        toTx.getAccountId(),
                        toTx.getTransactionType(),
                        toTx.getAmount(),
                        toTx.getCreatedAt()
                )
        ));

        String outDescription = "Transfer out " + amount.toPlainString() + " to account " + to.getId();
        publishAccountEventPort.publishAccountEvent(new DomainEvent(
                UUID.randomUUID(),
                correlationId,
                from.getId(),
                EventType.ACCOUNT_TRANSFER,
                now,
                outDescription,
                new AccountTransferredEventData(
                        from.getId(),
                        to.getId(),
                        amount,
                        TransferDirection.OUT,
                        outDescription
                )
        ));

        String inDescription = "Transfer in " + credited.toPlainString() + " from account " + from.getId();
        publishAccountEventPort.publishAccountEvent(new DomainEvent(
                UUID.randomUUID(),
                correlationId,
                to.getId(),
                EventType.ACCOUNT_TRANSFER,
                now,
                inDescription,
                new AccountTransferredEventData(
                        to.getId(),
                        from.getId(),
                        credited,
                        TransferDirection.IN,
                        inDescription
                )
        ));
    }
}
