package org.vmikhailov.java.cleanarchitecture.impl.transactions;

class CommandException extends RuntimeException {
    public CommandException(String message) {
        super(message);
    }

    public CommandException(Exception ex) {
        super(ex);
    }
}
