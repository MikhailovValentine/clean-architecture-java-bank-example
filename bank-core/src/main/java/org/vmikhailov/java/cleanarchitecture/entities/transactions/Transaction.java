package org.vmikhailov.java.cleanarchitecture.entities.transactions;

import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;

import java.util.Optional;


public interface Transaction<I, M extends Money<?>> {
    I getId();

    Optional<Account<I, M>> getFromAccount();

    Optional<Account<I, M>> getToAccount();

    M getAmount();

    OperationType getOperationType();

    TransactionState getState();
}
