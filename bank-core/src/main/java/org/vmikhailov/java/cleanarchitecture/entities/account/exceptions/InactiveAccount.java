package org.vmikhailov.java.cleanarchitecture.entities.account.exceptions;

import org.vmikhailov.java.cleanarchitecture.entities.account.Account;

public class InactiveAccount extends AccountException {
    private final Account account;

    public InactiveAccount(Account account) {
        this.account = account;
    }
}
