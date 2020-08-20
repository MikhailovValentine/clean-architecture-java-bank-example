package org.vmikhailov.java.cleanarchitecture.entities.client.exceptions;

public class ClientException extends RuntimeException {

    public ClientException() {
        super();
    }

    public ClientException(Throwable cause) {
        super(cause);
    }

    public ClientException(String s) {
        super(s);
    }
}
