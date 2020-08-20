package org.vmikhailov.java.cleanarchitecture.impl.entities.account;

import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.account.AccountData;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;

public class FiatAccountData extends AccountData<Long, FiatMoney> {
    public FiatAccountData(FiatMoney money, Client<Long> client) {
        super(money, client);
    }

    @Override
    public boolean test(Account<Long, FiatMoney> longFiatMoneyAccount) {
        return this.isSameData(longFiatMoneyAccount.getAccountData());
    }
}
