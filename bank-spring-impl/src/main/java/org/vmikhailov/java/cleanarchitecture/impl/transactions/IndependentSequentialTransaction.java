package org.vmikhailov.java.cleanarchitecture.impl.transactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class IndependentSequentialTransaction implements FiatTransactionCommand {
    private final List<FiatTransactionCommand> subCommands = new ArrayList<>();
    private final List<FiatTransactionCommand> executedCommands = new ArrayList<>();

    private FiatTransactionCommand currentCommand;

    public IndependentSequentialTransaction(FiatTransactionCommand... subCommands) {
        Collections.addAll(this.subCommands, subCommands);
    }

    @Override
    public void execute() {
        try {
            for (FiatTransactionCommand command : subCommands) {
                currentCommand = command;
                command.execute();
                executedCommands.add(command);
            }
            executedCommands.clear();
            currentCommand = null;
        } catch (Exception ex) {
            rollback();
        }
    }

    @Override
    public void rollback() {
        List<FiatTransactionCommand> revertingCommands;
        if (currentCommand != null) {
            revertingCommands = executedCommands;
        } else {
            revertingCommands = subCommands;
        }
        revertingCommands.forEach(FiatTransactionCommand::rollback);
    }
}
