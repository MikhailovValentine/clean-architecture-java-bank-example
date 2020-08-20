package org.vmikhailov.java.cleanarchitecture.impl.transactions;

interface FiatTransactionCommand {
    void execute();

    void rollback();
}
