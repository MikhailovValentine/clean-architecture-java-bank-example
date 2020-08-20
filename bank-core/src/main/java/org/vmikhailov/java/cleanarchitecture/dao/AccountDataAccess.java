package org.vmikhailov.java.cleanarchitecture.dao;

import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.account.AccountData;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;

import java.util.List;
import java.util.Optional;

public interface AccountDataAccess<I, M extends Money<?>> {
    Account<I, M> create(AccountData<I, M> data);

    void update(I identity, AccountData<I, M> data);

    void delete(I identity);

    Optional<Account<I, M>> get(I identity);

    List<Account<I, M>> getAll(Client<I> client);
}
