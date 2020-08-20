package org.vmikhailov.java.cleanarchitecture.impl.gateway;

import lombok.Setter;
import org.springframework.stereotype.Repository;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.OperationType;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.TransactionState;
import org.vmikhailov.java.cleanarchitecture.impl.dao.FiatTransactionDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransaction;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransactionData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTransactionGateway implements FiatTransactionDataAccess {
    private static final AtomicLong ID_SEQUENCE = new AtomicLong(0);
    private Map<Long, InMemoryTransaction> transactionStorage = new ConcurrentHashMap<>();

    @Override
    public FiatTransaction create(FiatTransactionData data) {
        if (data == null) {
            throw new NullPointerException();
        }
        long id = ID_SEQUENCE.incrementAndGet();
        InMemoryTransaction transaction = new InMemoryTransaction(id, data);
        transactionStorage.put(id, transaction);
        return transaction;
    }

    @Override
    public void updateTransactionState(Long id, TransactionState newState) {
        if (id == null) {
            throw new NullPointerException();
        }
        InMemoryTransaction transaction = transactionStorage.get(id);
        if (transaction == null) {
            throw new IllegalStateException();
        }
        transaction.setCurrentState(newState);
        transactionStorage.put(id, transaction);
    }

    @Override
    public Optional<FiatTransaction> get(Long id) {
        return Optional.ofNullable(transactionStorage.get(id));
    }

    @Override
    public List<FiatTransaction> getAll(Account<Long, FiatMoney> account) {
        return transactionStorage.values().stream().filter(t -> t.test(account)).collect(Collectors.toList());
    }

    private static class InMemoryTransaction implements FiatTransaction {
        final long id;
        final FiatTransactionData transactionData;
        @Setter
        TransactionState currentState;

        private InMemoryTransaction(long id, FiatTransactionData transactionData) {
            this.id = id;
            this.transactionData = transactionData;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public Optional<Account<Long, FiatMoney>> getFromAccount() {
            return transactionData.getAccountA();
        }

        @Override
        public Optional<Account<Long, FiatMoney>> getToAccount() {
            return transactionData.getAccountB();
        }

        @Override
        public FiatMoney getAmount() {
            return transactionData.getAmount();
        }

        @Override
        public OperationType getOperationType() {
            return transactionData.getOperationType();
        }

        @Override
        public TransactionState getState() {
            if (currentState == null) {
                return transactionData.getState();
            }
            return currentState;
        }

        @Override
        public boolean test(Account<Long, FiatMoney> longFiatMoneyAccount) {
            if (longFiatMoneyAccount == null) {
                return false;
            }
            Optional<Account<Long, FiatMoney>> fromAccount = getFromAccount();
            if (fromAccount.isPresent() && longFiatMoneyAccount.equals(fromAccount.get())) {
                return true;
            }
            Optional<Account<Long, FiatMoney>> toAccount = getToAccount();
            return toAccount.isPresent() && longFiatMoneyAccount.equals(toAccount.get());
        }
    }
}
