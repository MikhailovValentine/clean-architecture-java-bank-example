package org.vmikhailov.java.cleanarchitecture.entities.client;

import lombok.Getter;

public abstract class Client<I> {
    @Getter
    private final ClientData<I> clientData;

    protected Client(ClientData<I> clientData) {
        this.clientData = clientData;
    }

    public abstract I getId();

    public abstract boolean isActive();
}
