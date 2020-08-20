package org.vmikhailov.java.cleanarchitecture.impl.dao;

import org.vmikhailov.java.cleanarchitecture.dao.AccountDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;

public interface FiatAccountDataAccess extends AccountDataAccess<Long, FiatMoney> {
}
