package org.vmikhailov.java.cleanarchitecture.usecases;

import org.vmikhailov.java.cleanarchitecture.dao.AccountDataAccess;
import org.vmikhailov.java.cleanarchitecture.dao.ClientDataAccess;
import org.vmikhailov.java.cleanarchitecture.dao.TransactionDataAccess;
import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.account.AccountData;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountAlreadyExists;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountException;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountNotFound;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.InactiveAccount;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.entities.client.ClientData;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientAlreadyExist;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientDuplicatesFound;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientNotActive;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientNotExist;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.OperationType;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.Transaction;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.exceptions.TransactionException;
import org.vmikhailov.java.cleanarchitecture.transactions.TransactionService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class BankUseCases<I, M extends Money<?>, T extends Transaction<I, M>> {
    private final ClientDataAccess<I> clientDataAccess;
    private final AccountDataAccess<I, M> accountDataAccess;
    private final TransactionDataAccess<I, ?, M, T> transactionDataAccess;
    private final TransactionService<I, M, T> transactionService;

    public BankUseCases(ClientDataAccess<I> clientDataAccess,
                        AccountDataAccess<I, M> accountDataAccess,
                        TransactionDataAccess<I, ?, M, T> transactionDataAccess,
                        TransactionService<I, M, T> transactionService) {
        this.clientDataAccess = clientDataAccess;
        this.accountDataAccess = accountDataAccess;
        this.transactionDataAccess = transactionDataAccess;
        this.transactionService = transactionService;
    }

    /**
     * Client registered, providing his data for identification.
     *
     * @param data all mandatory client related information
     */
    public final Client<I> registerNewClient(ClientData<I> data) {
        Objects.requireNonNull(data);
        List<Client<I>> clients = clientDataAccess.getAll(data);
        if (!clients.isEmpty()) {
            throw new ClientAlreadyExist(clients.get(0));
        }
        return clientDataAccess.create(data);
    }

    /**
     * Searching all clients by some information form there profiles.
     *
     * @param data any information about client that can be used to search them in a storage
     * @return {@code Client}'s that matched to the given parameters
     */
    public final List<Client<I>> getClients(ClientData<I> data) {
        Objects.requireNonNull(data);
        List<Client<I>> clients = clientDataAccess.getAll(data);
        if (clients.isEmpty()) {
            throw new ClientNotExist();
        }
        return clients;
    }

    /**
     * Client asks to get his profile information, providing identification data.
     *
     * @param data unique identification information that should be related to only one client profile
     * @return {@code Client}'s that matched to the given parameters
     */
    public final Client<I> getClient(ClientData<I> data) {
        Objects.requireNonNull(data);
        List<Client<I>> clients = clientDataAccess.getAll(data);
        if (clients.isEmpty()) {
            throw new ClientNotExist();
        }
        if (clients.size() > 1) {
            throw new ClientDuplicatesFound();
        }
        return clients.get(0);
    }

    /**
     * Client stops been bank client - removes profile.
     *
     * @param clientId client identity
     */
    public final void deleteClient(I clientId) {
        Objects.requireNonNull(clientId);
        Optional<Client<I>> client = clientDataAccess.get(clientId);
        if (!client.isPresent()) {
            throw new ClientNotExist();
        }
        clientDataAccess.delete(clientId);
    }

    /**
     * Client creates new(or another) account in some currency.
     *
     * @param client      client object to whom link account
     * @param accountData all mandatory account related data
     */
    public final Account<I, M> createAccount(Client<I> client, AccountData<I, M> accountData) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(accountData);
        if (!client.isActive()) {
            throw new ClientNotActive();
        }
        Optional<? extends Account<I, M>> similarAccountExists = getAllAccounts(client)
                .stream()
                .filter(a -> a.getAccountData().isSameData(accountData)).findFirst();
        if (similarAccountExists.isPresent()) {
            throw new AccountAlreadyExists();
        }
        return accountDataAccess.create(accountData);
    }

    /**
     * Getting client account data.
     *
     * @param accountId account id
     */
    public final AccountData<I, M> getAccountData(I accountId) {
        Objects.requireNonNull(accountId);
        Optional<Account<I, M>> account = accountDataAccess.get(accountId);
        if (!account.isPresent()) {
            throw new AccountNotFound();
        }
        return account.get().getAccountData();
    }

    /**
     * Client adds some money to it's account.
     *
     * @param account {@code Account} to change value
     * @param money   amount of money to add to the {@code Account}
     */
    public final T replenishAccount(Account<I, M> account, M money) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(money);
        if (!account.isActive()) {
            throw new AccountNotFound();
        }
        try {
            return transactionService.make(account, money, OperationType.REPLENISH);
        } catch (TransactionException ex) {
            throw new AccountException(ex);
        }
    }

    /**
     * Client takes some money from the account.
     *
     * @param account {@code Account} to change value
     * @param money   amount of money to add to the {@code Account}
     */
    public final T withdrawFromAccount(Account<I, M> account, M money) {
        if (!account.isActive()) {
            throw new InactiveAccount(account);
        }
        try {
            return transactionService.make(account, money, OperationType.WITHDRAW);
        } catch (TransactionException ex) {
            throw new AccountException(ex);
        }
    }

    /**
     * Client transfers money from one account to another(his or other client) account.
     *
     * @param fromAccount {@code Account} to take money from
     * @param toAccount   {@code Account} to add money
     * @param money       amount of money to transfer
     */
    public final T transferMoney(Account<I, M> fromAccount, Account<I, M> toAccount, M money) {
        if (!fromAccount.isActive()) {
            throw new InactiveAccount(fromAccount);
        }
        if (!toAccount.isActive()) {
            throw new InactiveAccount(toAccount);
        }
        try {
            return transactionService.make(fromAccount, toAccount, money, OperationType.TRANSFER);
        } catch (TransactionException ex) {
            throw new AccountException(ex);
        }
    }

    /**
     * Client requests information about his accounts - amount of money on it and currency.
     *
     * @param client to identify owner of accounts
     * @return list of {@code Account}'s
     */
    public final List<Account<I, M>> getAllAccounts(Client<I> client) {
        return accountDataAccess.getAll(client);
    }

    /**
     * Client requests his operations - all transactions.
     *
     * @param account to identify source of transactions account
     * @return list of {@code Transaction}'s
     */
    public final List<T> getAllTransactions(Account<I, M> account) {
        return transactionDataAccess.getAll(account);
    }
}
