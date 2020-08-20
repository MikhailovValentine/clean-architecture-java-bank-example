package org.vmikhailov.java.cleanarchitecture.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public abstract class Money<T> {
    private final Currency currency;
    private final T amount;

    protected Money(Currency currency, T amount) {
        this.currency = currency;
        this.amount = amount;
    }
}
