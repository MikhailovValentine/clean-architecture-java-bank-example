package org.vmikhailov.java.cleanarchitecture.entities.account;

import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;

import java.util.function.Predicate;

public interface Account<I, M extends Money<?>> extends Predicate<Client<I>> {
    I getId();

    AccountData<I, M> getAccountData();

    boolean isActive();

    Client<I> getClient();

    void lock();

    void unlock();
}
