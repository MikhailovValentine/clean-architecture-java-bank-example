package org.vmikhailov.java.cleanarchitecture.impl.entities.transactions;

import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.OperationType;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.TransactionState;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;

import java.util.Optional;

public class FiatTransactionData {
    private final Account<Long, FiatMoney> accountA;
    private final Account<Long, FiatMoney> accountB;
    private final FiatMoney amount;
    private final OperationType operationType;

    public FiatTransactionData(Account<Long, FiatMoney> accountA, Account<Long, FiatMoney> accountB, FiatMoney amount, OperationType operationType) {
        this.accountA = accountA;
        this.accountB = accountB;
        this.amount = amount;
        this.operationType = operationType;
    }

    public Optional<Account<Long, FiatMoney>> getAccountA() {
        return Optional.ofNullable(accountA);
    }

    public Optional<Account<Long, FiatMoney>> getAccountB() {
        return Optional.ofNullable(accountB);
    }

    public FiatMoney getAmount() {
        return amount;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public TransactionState getState() {
        return TransactionState.STARTED;
    }
}
