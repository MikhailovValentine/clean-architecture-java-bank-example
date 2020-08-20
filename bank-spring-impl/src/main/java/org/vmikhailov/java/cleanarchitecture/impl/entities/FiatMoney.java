package org.vmikhailov.java.cleanarchitecture.impl.entities;

import org.vmikhailov.java.cleanarchitecture.entities.Currency;
import org.vmikhailov.java.cleanarchitecture.entities.Money;

import java.math.BigDecimal;

public class FiatMoney extends Money<BigDecimal> {
    public FiatMoney(Currency currency, BigDecimal amount) {
        super(currency, amount);
    }
}
