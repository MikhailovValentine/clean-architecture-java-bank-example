package org.vmikhailov.java.cleanarchitecture.impl.gateway;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Repository;
import org.vmikhailov.java.cleanarchitecture.dao.ClientDataAccess;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.account.AccountData;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountException;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountNotFound;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.LockedException;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.impl.dao.FiatAccountDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Repository
public class InMemoryAccountGateway implements FiatAccountDataAccess {
    private static final AtomicLong ID_SEQUENCE = new AtomicLong(0);
    private final ClientDataAccess<Long> clientStorage;
    private Map<Long, InMemoryAccount> accountStorage = new ConcurrentHashMap<>();

    public InMemoryAccountGateway(ClientDataAccess<Long> clientStorage) {
        this.clientStorage = clientStorage;
    }

    @Override
    public Account<Long, FiatMoney> create(AccountData<Long, FiatMoney> data) {
        if (data == null) {
            throw new NullPointerException();
        }
        long id = ID_SEQUENCE.incrementAndGet();
        InMemoryAccount inMemoryAccount = new InMemoryAccount(id, data, data.getClient().getId());
        accountStorage.put(id, inMemoryAccount);
        return inMemoryAccount;
    }

    @Override
    public void update(Long id, AccountData<Long, FiatMoney> data) {
        if (id == null) {
            throw new AccountNotFound();
        }
        if (data == null) {
            throw new AccountException();
        }
        InMemoryAccount account = accountStorage.get(id);
        if (account == null) {
            throw new AccountNotFound();
        }

        Lock lock = account.getLock();
        lock.lock();
        try {
            InMemoryAccount replacedAccount = accountStorage.put(id,
                    new InMemoryAccount(id, data, data.getClient().getId(), lock));
            if (replacedAccount != account) {
                throw new AccountException();
            }
        } catch (RuntimeException ex) {
            throw new AccountException(ex);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(Long id) {
        InMemoryAccount account = accountStorage.get(id);
        if (account == null) {
            throw new AccountNotFound();
        }
        account.lock();
        try {
            account.setActive(false);
        } finally {
            account.unlock();
        }
    }

    @Override
    public Optional<Account<Long, FiatMoney>> get(Long id) {
        return Optional.ofNullable(accountStorage.get(id));
    }

    @Override
    public List<Account<Long, FiatMoney>> getAll(Client<Long> client) {
        return accountStorage.values().stream().filter(a -> a.test(client)).collect(Collectors.toList());
    }

    @Getter
    private class InMemoryAccount implements Account<Long, FiatMoney> {
        final Long id;
        @Getter
        final AccountData<Long, FiatMoney> accountData;
        final Long clientId;
        final Lock lock;
        @Setter
        boolean isActive = true;

        private InMemoryAccount(Long id, AccountData<Long, FiatMoney> accountData, Long clientId, Lock lock) {
            this.id = id;
            this.accountData = accountData;
            this.clientId = clientId;
            this.lock = lock;
        }

        private InMemoryAccount(Long id, AccountData<Long, FiatMoney> accountData, Long clientId) {
            this(id, accountData, clientId, new ReentrantLock());
        }

        @Override
        public boolean isActive() {
            return isActive && clientStorage.get(clientId).isPresent();
        }

        @Override
        public Client<Long> getClient() {
            return clientStorage.get(clientId).orElse(null);
        }

        @Override
        public void lock() {
            if (!lock.tryLock()) {
                throw new LockedException();
            }
        }

        @Override
        public void unlock() {
            lock.unlock();
        }

        @Override
        public boolean test(Client<Long> longClient) {
            if (longClient == null) {
                return false;
            }
            return Objects.equals(longClient.getId(), clientId);
        }
    }
}
