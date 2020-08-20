package org.vmikhailov.java.cleanarchitecture.impl.transactions;

import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

class AddAbstractIndependentFiatTransactionCommand extends AbstractIndependentFiatTransactionCommand<BigDecimal> {
    private BigDecimal augend;

    public AddAbstractIndependentFiatTransactionCommand(Supplier<BigDecimal> initialState,
                                                        Consumer<BigDecimal> commandReceiver,
                                                        BigDecimal amount) {
        super(initialState, commandReceiver);
        augend = amount;
    }

    @Override
    protected UnaryOperator<BigDecimal> getCommandOperator() {
        return addend -> addend.add(augend);
    }
}
