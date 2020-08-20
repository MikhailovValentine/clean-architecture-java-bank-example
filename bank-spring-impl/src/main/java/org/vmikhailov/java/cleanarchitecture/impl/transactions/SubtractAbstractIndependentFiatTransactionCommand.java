package org.vmikhailov.java.cleanarchitecture.impl.transactions;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.lang.String.format;

class SubtractAbstractIndependentFiatTransactionCommand extends AbstractIndependentFiatTransactionCommand<BigDecimal> {
    private BigDecimal subtrahendAmount;

    public SubtractAbstractIndependentFiatTransactionCommand(Supplier<BigDecimal> initialState,
                                                             Consumer<BigDecimal> commandReceiver,
                                                             BigDecimal amount) {
        super(initialState, commandReceiver);
        subtrahendAmount = amount;
    }

    @Override
    protected UnaryOperator<BigDecimal> getCommandOperator() {
        return this::subtractOperation;
    }

    private BigDecimal subtractOperation(BigDecimal initialAmount) {
        if (initialAmount == null) {
            throw new CommandException("initial amount is empty");
        }
        if (initialAmount.compareTo(subtrahendAmount) < 0) {
            throw new CommandException(format("initial amount:%s less then required amount:%s", initialAmount, subtrahendAmount));
        }
        return initialAmount.subtract(subtrahendAmount);
    }
}
