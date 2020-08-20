package org.vmikhailov.java.cleanarchitecture.impl.dao;

import org.vmikhailov.java.cleanarchitecture.dao.TransactionDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransaction;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransactionData;

public interface FiatTransactionDataAccess extends TransactionDataAccess<Long, FiatTransactionData, FiatMoney, FiatTransaction> {
}
