package org.vmikhailov.java.cleanarchitecture.entities.client.exceptions;

import org.vmikhailov.java.cleanarchitecture.entities.client.Client;

public class ClientAlreadyExist extends ClientException {
    private final Client<?> client;

    public ClientAlreadyExist(Client<?> client) {
        this.client = client;
    }
}
