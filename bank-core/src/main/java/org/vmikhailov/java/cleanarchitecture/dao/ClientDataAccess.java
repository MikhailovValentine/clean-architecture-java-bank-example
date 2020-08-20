package org.vmikhailov.java.cleanarchitecture.dao;

import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.entities.client.ClientData;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface ClientDataAccess<I> {
    Client<I> create(ClientData<I> data);

    void delete(I identity);

    Optional<Client<I>> get(I identity);

    List<Client<I>> getAll(Predicate<Client<I>> params);
}
