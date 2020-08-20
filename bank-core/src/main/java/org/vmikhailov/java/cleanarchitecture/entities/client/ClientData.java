package org.vmikhailov.java.cleanarchitecture.entities.client;

import java.util.function.Predicate;

public interface ClientData<I> extends Predicate<Client<I>> {
}
