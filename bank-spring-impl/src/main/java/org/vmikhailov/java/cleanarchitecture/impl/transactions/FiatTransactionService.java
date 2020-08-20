package org.vmikhailov.java.cleanarchitecture.impl.transactions;

import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransaction;
import org.vmikhailov.java.cleanarchitecture.transactions.TransactionService;

public interface FiatTransactionService extends TransactionService<Long, FiatMoney, FiatTransaction> {
}
