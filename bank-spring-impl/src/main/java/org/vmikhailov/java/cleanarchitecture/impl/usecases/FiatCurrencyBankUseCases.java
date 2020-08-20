package org.vmikhailov.java.cleanarchitecture.impl.usecases;

import org.springframework.stereotype.Service;
import org.vmikhailov.java.cleanarchitecture.dao.ClientDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.dao.FiatAccountDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.dao.FiatTransactionDataAccess;
import org.vmikhailov.java.cleanarchitecture.impl.entities.FiatMoney;
import org.vmikhailov.java.cleanarchitecture.impl.entities.transactions.FiatTransaction;
import org.vmikhailov.java.cleanarchitecture.impl.transactions.FiatTransactionService;
import org.vmikhailov.java.cleanarchitecture.usecases.BankUseCases;

@Service
public class FiatCurrencyBankUseCases extends BankUseCases<Long, FiatMoney, FiatTransaction> {
    FiatCurrencyBankUseCases(ClientDataAccess<Long> clientDataAccess,
                             FiatAccountDataAccess accountDataAccess,
                             FiatTransactionDataAccess transactionDataAccess,
                             FiatTransactionService transactionService) {
        super(clientDataAccess, accountDataAccess, transactionDataAccess, transactionService);
    }
}
