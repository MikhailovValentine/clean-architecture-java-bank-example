package org.vmikhailov.java.cleanarchitecture.transactions;

import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.OperationType;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.Transaction;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.exceptions.TransactionException;

public interface TransactionService<I, M extends Money<?>, T extends Transaction<I, M>> {
    T make(Account<I, M> account, M money, OperationType type) throws TransactionException;

    T make(Account<I, M> fromAccount, Account<I, M> toAccount, M money, OperationType type) throws TransactionException;
}
