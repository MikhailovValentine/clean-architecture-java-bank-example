package org.vmikhailov.java.cleanarchitecture.impl.entities.transactions;

import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.Transaction;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;

import java.util.function.Predicate;

public interface FiatTransaction extends Transaction<Long, FiatMoney>, Predicate<Account<Long, FiatMoney>> {
}
