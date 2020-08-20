package org.vmikhailov.java.cleanarchitecture.impl.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;
import org.vmikhailov.java.cleanarchitecture.dao.ClientDataAccess;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.entities.client.ClientData;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientNotExist;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class InMemoryClientGateway implements ClientDataAccess<Long> {
    private static final AtomicLong ID_SEQUENCE = new AtomicLong(0);
    private Map<Long, InMemoryClient> clientStorage = new ConcurrentHashMap<>();

    @Override
    public Client<Long> create(ClientData<Long> data) {
        if (data == null) {
            throw new NullPointerException();
        }
        long id = ID_SEQUENCE.incrementAndGet();
        InMemoryClient client = new InMemoryClient(id, data);
        clientStorage.put(id, client);
        return client;
    }

    @Override
    public void delete(Long id) {
        InMemoryClient client = clientStorage.get(id);
        if (client == null) {
            throw new ClientNotExist();
        }
        client.setActive(false);
    }

    @Override
    public Optional<Client<Long>> get(Long id) {
        InMemoryClient inMemoryClient = clientStorage.get(id);
        if (inMemoryClient != null && inMemoryClient.isActive()) {
            return Optional.of(inMemoryClient);
        }
        return Optional.empty();
    }

    @Override
    public List<Client<Long>> getAll(Predicate<Client<Long>> searchParams) {
        return clientStorage.values().stream().filter(Client::isActive).filter(searchParams).collect(Collectors.toList());
    }

    @Getter
    private static class InMemoryClient extends Client<Long> {
        final Long id;
        @Setter
        boolean isActive = true;

        private InMemoryClient(Long id, ClientData<Long> data) {
            super(data);
            Objects.requireNonNull(id);
            this.id = id;
        }

        @Override
        public boolean isActive() {
            return isActive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InMemoryClient that = (InMemoryClient) o;
            if (id.equals(that.id)) {
                return true;
            }
            return Objects.equals(getClientData(), that.getClientData());
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, getClientData());
        }
    }
}
