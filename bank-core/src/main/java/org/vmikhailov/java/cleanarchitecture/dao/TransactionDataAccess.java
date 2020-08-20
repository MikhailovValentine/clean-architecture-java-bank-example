package org.vmikhailov.java.cleanarchitecture.dao;

import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.Transaction;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.TransactionState;

import java.util.List;
import java.util.Optional;

public interface TransactionDataAccess<I, D, M extends Money<?>, T extends Transaction<I, M>> {
    T create(D data);

    void updateTransactionState(I identity, TransactionState newState);

    Optional<T> get(I identity);

    List<T> getAll(Account<I, M> account);
}
