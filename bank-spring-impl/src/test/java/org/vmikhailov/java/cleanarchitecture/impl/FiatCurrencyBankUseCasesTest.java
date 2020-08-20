package org.vmikhailov.java.cleanarchitecture.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vmikhailov.java.cleanarchitecture.entities.Currency;
import org.vmikhailov.java.cleanarchitecture.entities.account.Account;
import org.vmikhailov.java.cleanarchitecture.entities.account.AccountData;
import org.vmikhailov.java.cleanarchitecture.entities.account.exceptions.AccountAlreadyExists;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.entities.client.ClientData;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientAlreadyExist;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientDuplicatesFound;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientNotActive;
import org.vmikhailov.java.cleanarchitecture.entities.client.exceptions.ClientNotExist;
import org.vmikhailov.java.cleanarchitecture.entities.transactions.TransactionState;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;
import org.vmikhailov.java.cleanarchitecture.impl.entities.account.FiatAccountData;
import org.vmikhailov.java.cleanarchitecture.impl.entities.client.ClientDocument;
import org.vmikhailov.java.cleanarchitecture.impl.entities.client.UniversalBankClientData;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransaction;
import org.vmikhailov.java.cleanarchitecture.impl.usecases.FiatCurrencyBankUseCases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = Application.class)
public class FiatCurrencyBankUseCasesTest {
    private static final Currency TEST_CURRENCY = new Currency() {
        @Override
        public boolean isSame(Currency other) {
            return this == other;
        }
    };

    @Autowired
    FiatCurrencyBankUseCases useCases;

    @Test
    public void test_register_new_client_and_gets_same() {
        assertNotNull(useCases);

        ClientData<Long> generatedClientData = generateRandomClientData();
        Client<Long> newCreatedClient = useCases.registerNewClient(generatedClientData);

        assertNotNull(newCreatedClient);

        Client<Long> foundClient = useCases.getClient(generatedClientData);

        assertNotNull(foundClient);
        assertEquals(newCreatedClient.getId(), foundClient.getId());
        assertEquals(newCreatedClient.getClientData(), generatedClientData);
    }

    @Test
    public void test_register_client_with_same_data() {
        ClientData<Long> generatedClientData = generateRandomClientData();
        useCases.registerNewClient(generatedClientData);

        Assertions.assertThrows(ClientAlreadyExist.class, () -> useCases.registerNewClient(generatedClientData));
    }

    @Test
    public void test_get_clients() {
        ClientData<Long> generatedClientData = generateRandomClientData();
        Client<Long> createdClient = useCases.registerNewClient(generatedClientData);

        List<Client<Long>> clients = useCases.getClients(generatedClientData);
        assertNotNull(clients);
        assertEquals(1, clients.size());

        Client<Long> foundClient = clients.get(0);
        assertSame(createdClient, foundClient);
    }

    @Test
    public void test_get_client_exception_if_not_exists() {
        ClientData<Long> generatedClientData = generateRandomClientData();

        Assertions.assertThrows(ClientNotExist.class, () -> useCases.getClient(generatedClientData));
    }

    @Test
    public void test_get_clients_exception_if_not_exists() {
        ClientData<Long> generatedClientData = generateRandomClientData();

        Assertions.assertThrows(ClientNotExist.class, () -> useCases.getClients(generatedClientData));
    }

    @Test
    public void test_get_clients_returns_all_clients() {
        Client<Long> client1 = useCases.registerNewClient(generateRandomClientData());
        Client<Long> client2 = useCases.registerNewClient(generateRandomClientData());

        ClientData<Long> anyClientMatchClientData = o -> true;
        List<Client<Long>> clients = useCases.getClients(anyClientMatchClientData);

        assertTrue(clients.contains(client1));
        assertTrue(clients.contains(client2));
    }

    @Test
    public void test_get_client_exception_if_many_clients_exists() {
        useCases.registerNewClient(generateRandomClientData());
        useCases.registerNewClient(generateRandomClientData());

        ClientData<Long> anyClientMatchClientData = o -> true;

        Assertions.assertThrows(ClientDuplicatesFound.class, () -> useCases.getClient(anyClientMatchClientData));
    }

    @Test
    public void test_delete_client() {
        ClientData<Long> existentClientData = generateRandomClientData();
        Client<Long> existentClient = useCases.registerNewClient(existentClientData);
        assertNotNull(existentClient);
        assertNotNull(existentClient.getId());

        useCases.deleteClient(existentClient.getId());

        Assertions.assertThrows(ClientNotExist.class, () -> useCases.getClient(existentClientData));
    }

    @Test
    public void test_delete_client_non_exiting_client_throws_exception() {
        Assertions.assertThrows(ClientNotExist.class, () -> useCases.deleteClient(99999_9999L));
    }

    @Test
    public void test_create_new_account() {
        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        FiatAccountData accountData = generateRandomAccountDataForClient(BigDecimal.ZERO, existentClient);

        useCases.createAccount(existentClient, accountData);

        List<Account<Long, FiatMoney>> allAccounts = useCases.getAllAccounts(existentClient);
        assertNotNull(allAccounts);
        assertEquals(1, allAccounts.size());

        Account<Long, FiatMoney> longFiatMoneyAccount = allAccounts.get(0);
        assertNotNull(longFiatMoneyAccount);

        Client<Long> clientFromAccount = longFiatMoneyAccount.getClient();
        assertNotNull(clientFromAccount);
        assertSame(existentClient, clientFromAccount);

        AccountData<Long, FiatMoney> accountDataFromAccount = longFiatMoneyAccount.getAccountData();
        assertNotNull(accountDataFromAccount);
        assertSame(accountData, accountDataFromAccount);
    }

    @Test
    public void test_create_new_account_for_inactive_client() {
        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        assertNotNull(existentClient.getId());
        useCases.deleteClient(existentClient.getId());

        FiatAccountData accountData = generateRandomAccountDataForClient(BigDecimal.ZERO, existentClient);
        assertThrows(ClientNotActive.class, () -> useCases.createAccount(existentClient, accountData));
    }

    @Test
    public void test_create_new_account_with_same_account_data() {
        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        FiatAccountData accountData = generateRandomAccountDataForClient(BigDecimal.ZERO, existentClient);
        Account<Long, FiatMoney> account = useCases.createAccount(existentClient, accountData);
        assertNotNull(account);

        assertThrows(AccountAlreadyExists.class, () -> useCases.createAccount(existentClient, accountData));
    }

    @Test
    public void test_get_account_data() {
        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        FiatAccountData accountData = generateRandomAccountDataForClient(existentClient);

        Account<Long, FiatMoney> createdAccount = useCases.createAccount(existentClient, accountData);

        assertNotNull(createdAccount);
        AccountData<Long, FiatMoney> accountDataFromAccount = createdAccount.getAccountData();

        assertNotNull(accountDataFromAccount);
        assertSame(accountData, accountDataFromAccount);

        AccountData<Long, FiatMoney> accountDataFromService = useCases.getAccountData(createdAccount.getId());
        assertNotNull(accountDataFromService);
        assertSame(accountData, accountDataFromService);
    }

    @ParameterizedTest
    @MethodSource("randomTestMoney")
    public void test_replenish_account(FiatMoney amount) {
        assertNotNull(amount);

        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        FiatAccountData accountData = generateRandomAccountDataForClient(BigDecimal.ONE, existentClient);

        Account<Long, FiatMoney> createdAccount = useCases.createAccount(existentClient, accountData);
        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getAccountData());
        assertNotNull(createdAccount.getAccountData().getMoney());

        FiatTransaction transaction = useCases.replenishAccount(createdAccount, amount);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertTrue(transaction.getFromAccount().isPresent());
        assertFalse(transaction.getToAccount().isPresent());
        assertSame(TransactionState.COMMITTED, transaction.getState());
        assertSame(amount, transaction.getAmount());

        BigDecimal expectedAmount = BigDecimal.ONE.add(amount.getAmount());
        assertEquals(expectedAmount, createdAccount.getAccountData().getMoney().getAmount());
    }

    @ParameterizedTest
    @MethodSource("randomTestMoney")
    public void test_withdraw_account(FiatMoney amount) {
        assertNotNull(amount);

        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        BigDecimal initialAmount = amount.getAmount().multiply(BigDecimal.TEN);
        FiatAccountData accountData = generateRandomAccountDataForClient(initialAmount, existentClient);

        Account<Long, FiatMoney> createdAccount = useCases.createAccount(existentClient, accountData);
        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getAccountData());
        assertNotNull(createdAccount.getAccountData().getMoney());

        FiatTransaction transaction = useCases.withdrawFromAccount(createdAccount, amount);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertTrue(transaction.getFromAccount().isPresent());
        assertFalse(transaction.getToAccount().isPresent());
        assertSame(TransactionState.COMMITTED, transaction.getState());
        assertSame(amount, transaction.getAmount());

        BigDecimal expectedAmount = initialAmount.subtract(amount.getAmount());
        assertEquals(expectedAmount, createdAccount.getAccountData().getMoney().getAmount());
    }

    @ParameterizedTest
    @MethodSource("randomTestMoney")
    public void test_transfer_money(FiatMoney amount) {
        assertNotNull(amount);

        Client<Long> existentClient1 = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient1);
        Client<Long> existentClient2 = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient2);

        BigDecimal initialAmountAtTheClient1 = amount.getAmount().multiply(BigDecimal.TEN);
        FiatAccountData accountData1 = generateRandomAccountDataForClient(initialAmountAtTheClient1, existentClient1);
        FiatAccountData accountData2 = generateRandomAccountDataForClient(BigDecimal.ONE, existentClient1);

        Account<Long, FiatMoney> createdAccount1 = useCases.createAccount(existentClient1, accountData1);
        assertNotNull(createdAccount1);
        assertNotNull(createdAccount1.getAccountData());
        assertNotNull(createdAccount1.getAccountData().getMoney());
        Account<Long, FiatMoney> createdAccount2 = useCases.createAccount(existentClient2, accountData2);
        assertNotNull(createdAccount2);
        assertNotNull(createdAccount2.getAccountData());
        assertNotNull(createdAccount2.getAccountData().getMoney());

        FiatTransaction transaction = useCases.transferMoney(createdAccount1, createdAccount2, amount);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertTrue(transaction.getFromAccount().isPresent());
        assertTrue(transaction.getToAccount().isPresent());
        assertSame(createdAccount1, transaction.getFromAccount().get());
        assertSame(createdAccount2, transaction.getToAccount().get());
        assertSame(TransactionState.COMMITTED, transaction.getState());
        assertSame(amount, transaction.getAmount());

        BigDecimal expectedAmount1 = initialAmountAtTheClient1.subtract(amount.getAmount());
        assertEquals(expectedAmount1, createdAccount1.getAccountData().getMoney().getAmount());
        BigDecimal expectedAmount2 = BigDecimal.ONE.add(amount.getAmount());
        assertEquals(expectedAmount2, createdAccount2.getAccountData().getMoney().getAmount());
    }

    private static Stream<Arguments> randomTestMoney() {
        return DoubleStream.generate(() -> ThreadLocalRandom.current().nextDouble(1_000_000_000_000d))
                .limit(1000)
                .mapToObj(BigDecimal::new)
                .map(n -> Arguments.of(generateFiatMoney(n)));
    }

    @Test
    public void test_get_all_accounts() {
        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        List<Account<Long, FiatMoney>> allAccounts = useCases.getAllAccounts(existentClient);
        assertNotNull(allAccounts);
        assertTrue(allAccounts.isEmpty());

        Account<Long, FiatMoney> createdAccount = useCases.createAccount(existentClient, generateRandomAccountDataForClient(existentClient));
        allAccounts = useCases.getAllAccounts(existentClient);
        assertNotNull(allAccounts);
        assertEquals(1, allAccounts.size());
        assertSame(createdAccount, allAccounts.get(0));

        Account<Long, FiatMoney> createdAccount2 = useCases.createAccount(existentClient,
                new FiatAccountData(new FiatMoney(new Currency() {
                    @Override
                    public boolean isSame(Currency other) {
                        return this == other;
                    }
                }, BigDecimal.ONE), existentClient));
        allAccounts = useCases.getAllAccounts(existentClient);
        assertNotNull(allAccounts);
        assertEquals(2, allAccounts.size());
        assertTrue(allAccounts.contains(createdAccount));
        assertTrue(allAccounts.contains(createdAccount2));
    }

    @Test
    public void test_get_all_transactions() {
        Client<Long> existentClient = useCases.registerNewClient(generateRandomClientData());
        assertNotNull(existentClient);

        FiatAccountData accountData = generateRandomAccountDataForClient(existentClient);
        Account<Long, FiatMoney> createdAccount = useCases.createAccount(existentClient, accountData);
        assertNotNull(createdAccount);
        assertNotNull(createdAccount.getAccountData());
        assertNotNull(createdAccount.getAccountData().getMoney());

        BigDecimal amount = BigDecimal.ONE;
        FiatTransaction transaction1 = useCases.replenishAccount(createdAccount, generateFiatMoney(amount));
        FiatTransaction transaction2 = useCases.replenishAccount(createdAccount, generateFiatMoney(amount));
        FiatTransaction transaction3 = useCases.withdrawFromAccount(createdAccount, generateFiatMoney(amount));

        List<FiatTransaction> allTransactions = useCases.getAllTransactions(createdAccount);
        assertNotNull(allTransactions);
        assertEquals(3, allTransactions.size());
        assertTrue(allTransactions.contains(transaction1));
        assertTrue(allTransactions.contains(transaction2));
        assertTrue(allTransactions.contains(transaction3));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_check_for_required_not_null_arguments_works() {
        Assertions.assertThrows(NullPointerException.class, () -> useCases.deleteClient(null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.registerNewClient(null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.getClient(null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.getClients(null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.createAccount(null, null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.createAccount(mock(Client.class), null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.getAccountData(null));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.replenishAccount(null, generateFiatMoney(null)));
        Assertions.assertThrows(NullPointerException.class, () -> useCases.replenishAccount(mock(Account.class), null));
    }

    private static ClientData<Long> generateRandomClientData() {
        ClientDocument randomDataDocument = ClientDocument.builder()
                .clientName(randomString())
                .clientSurname(randomString())
                .documentNumber(randomString())
                .documentType(ClientDocument.DocumentType.PASSPORT)
                .build();
        return UniversalBankClientData.builder()
                .name("test_name")
                .surname("test_surname")
                .birthDate(LocalDate.now())
                .documents(Collections.singletonList(randomDataDocument))
                .build();
    }

    private static FiatAccountData generateRandomAccountDataForClient(BigDecimal money, Client<Long> client) {
        return new FiatAccountData(generateFiatMoney(money), client);
    }

    private static FiatAccountData generateRandomAccountDataForClient(Client<Long> client) {
        return new FiatAccountData(generateFiatMoney(null), client);
    }

    private static FiatMoney generateFiatMoney(BigDecimal money) {
        if (money == null) {
            money = new BigDecimal(ThreadLocalRandom.current().nextLong(0, 1000000L));
        }
        return new FiatMoney(TEST_CURRENCY, money);
    }

    private static String randomString() {
        return UUID.randomUUID().toString();
    }
}