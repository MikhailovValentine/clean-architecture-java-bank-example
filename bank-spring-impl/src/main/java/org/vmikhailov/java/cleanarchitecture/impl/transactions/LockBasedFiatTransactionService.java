package org.vmikhailov.java.cleanarchitecture.impl.transactions;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.vmikhailov.java.cleanarchitecture.entities.Currency;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.account.AccountData;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountException;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.OperationType;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.TransactionState;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.exceptions.TransactionException;
import org.vmikhailov.java.cleanarchitecture.impl.dao.FiatAccountDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.dao.FiatTransactionDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransaction;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransactionData;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/*
 * Why this service here. It is solely business service and has complicated enough logic to emulate
 * complex business services to have an ability to incorporate such service in clear architecture - architectural style.
 *
 * Principles this transaction system build:
 *  there are several basic operations like transfer from A to B and add to A or withdraw from A
 *  all other operation are build from those basic ones.
 *
 * Not implemented but should be straightforward enhancement if would be done in practice:
 *  dynamic/complex command implementation based on sequences of simple commands.
 */
@Service
public class LockBasedFiatTransactionService implements FiatTransactionService {
    private final FiatTransactionDataAccess transactionDataAccess;
    private final FiatAccountDataAccess accountDataAccess;

    public LockBasedFiatTransactionService(FiatTransactionDataAccess transactionDataAccess,
                                           FiatAccountDataAccess accountDataDataAccess) {
        this.transactionDataAccess = transactionDataAccess;
        this.accountDataAccess = accountDataDataAccess;
    }

    /**
     * Here should be some reasonable checks.
     *
     * @param money - money to check
     */
    private static void checkMoney(FiatMoney money) {
        if (money.getAmount() == null) {
            throw new IllegalArgumentException();
        }
        if (money.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException();
        }
    }

    private static void checkCurrencyUniform(FiatMoney moneyA, FiatMoney moneyB) {
        checkCurrencyUniform(moneyA, moneyB, null);
    }

    private static void checkCurrencyUniform(FiatMoney moneyA, FiatMoney moneyB, FiatMoney moneyC) {
        Currency currencyA = moneyA.getCurrency();
        if (!currencyA.isSame(moneyB.getCurrency()) || (moneyC != null && !currencyA.isSame(moneyC.getCurrency()))) {
            throw new TransactionException("not working with different currency");
        }
    }

    private static Supplier<BigDecimal> getMoneyAmountState(@NonNull Account<Long, FiatMoney> account) {
        FiatMoney money = account.getAccountData().getMoney();
        return money::getAmount;
    }

    @SafeVarargs
    private static void sortAndLockAccounts(Account<Long, FiatMoney>... accounts) {
        Stream.of(accounts).filter(Objects::nonNull).sorted(Comparator.comparingLong(Account::getId)).forEach(Account::lock);
    }

    private static void unlockAccounts(Account<?, ?>... accounts) {
        Stream.of(accounts).filter(Objects::nonNull).forEach(Account::unlock);
    }

    @Override
    public FiatTransaction make(Account<Long, FiatMoney> account, FiatMoney money, OperationType type) throws TransactionException {
        checkMoney(money);
        checkCurrencyUniform(account.getAccountData().getMoney(), money);
        FiatTransactionCommand command = getCommand(type, money, account, null);
        return commandInvoker(command, money, type, account, null);
    }

    @Override
    public FiatTransaction make(Account<Long, FiatMoney> fromAccount,
                                Account<Long, FiatMoney> toAccount,
                                FiatMoney money, OperationType type) {
        checkMoney(money);
        checkCurrencyUniform(fromAccount.getAccountData().getMoney(), toAccount.getAccountData().getMoney(), money);
        FiatTransactionCommand command = getCommand(type, money, fromAccount, toAccount);
        return commandInvoker(command, money, type, fromAccount, toAccount);
    }

    private FiatTransactionCommand getCommand(@NonNull OperationType operationType,
                                              @NonNull FiatMoney money,
                                              @NonNull Account<Long, FiatMoney> accountA,
                                              Account<Long, FiatMoney> accountB) {
        BigDecimal amount = money.getAmount();
        switch (operationType) {
            case REPLENISH:
                return new AddAbstractIndependentFiatTransactionCommand(getMoneyAmountState(accountA), getCommandResultConsumer(accountA), amount);
            case WITHDRAW:
                return new SubtractAbstractIndependentFiatTransactionCommand(getMoneyAmountState(accountA), getCommandResultConsumer(accountA), amount);
            case TRANSFER:
                Objects.requireNonNull(accountB);
                return new IndependentSequentialTransaction(
                        new SubtractAbstractIndependentFiatTransactionCommand(getMoneyAmountState(accountA), getCommandResultConsumer(accountA), amount),
                        new AddAbstractIndependentFiatTransactionCommand(getMoneyAmountState(accountB), getCommandResultConsumer(accountB), amount));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Consumer<BigDecimal> getCommandResultConsumer(@NonNull Account<Long, FiatMoney> account) {
        AccountData<Long, FiatMoney> accountData = account.getAccountData();
        FiatMoney money = account.getAccountData().getMoney();
        return newAmount -> {
            accountData.setMoney(new FiatMoney(money.getCurrency(), newAmount));
            accountDataAccess.update(account.getId(), account.getAccountData());
        };
    }

    private FiatTransaction commandInvoker(@NonNull FiatTransactionCommand command,
                                           @NonNull FiatMoney money,
                                           @NonNull OperationType type,
                                           @NonNull Account<Long, FiatMoney> accountA,
                                           Account<Long, FiatMoney> accountB) {
        FiatTransaction transaction = createTransaction(money, type, accountA, accountB);
        sortAndLockAccounts(accountA, accountB);
        try {
            command.execute();
            transactionDataAccess.updateTransactionState(transaction.getId(), TransactionState.COMMITTED);
        } catch (AccountException | TransactionException ex) {
            command.rollback();
            transactionDataAccess.updateTransactionState(transaction.getId(), TransactionState.ROLLBACKED);
            throw new TransactionException(ex);
        } finally {
            unlockAccounts(accountA, accountB);
        }
        return transaction;
    }

    private FiatTransaction createTransaction(FiatMoney money, OperationType type,
                                              Account<Long, FiatMoney> accountA,
                                              Account<Long, FiatMoney> accountB) {
        FiatTransactionData transactionData = new FiatTransactionData(accountA, accountB, money, type);
        return transactionDataAccess.create(transactionData);
    }
}
