package org.vmikhailov.java.cleanarchitecture.impl.transactions;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@Getter
abstract class AbstractIndependentFiatTransactionCommand<T> implements FiatTransactionCommand {
    private final Supplier<T> initialState;
    private final Consumer<T> commandReceiver;

    private AtomicBoolean canRevert = new AtomicBoolean(false);
    private T beforeExecute;

    AbstractIndependentFiatTransactionCommand(Supplier<T> initialState,
                                              Consumer<T> commandReceiver) {
        this.initialState = initialState;
        this.commandReceiver = commandReceiver;
    }

    public void execute() {
        beforeExecute = initialState.get();
        try {
            T afterExecute = getCommandOperator().apply(beforeExecute);
            commandReceiver.accept(afterExecute);
            canRevert.set(true);
        } catch (Exception ex) {
            throw new CommandException(ex);
        }
    }

    public void rollback() {
        if (canRevert.get()) {
            commandReceiver.accept(beforeExecute);
            canRevert.set(false);
        }
    }

    protected abstract UnaryOperator<T> getCommandOperator();
}
