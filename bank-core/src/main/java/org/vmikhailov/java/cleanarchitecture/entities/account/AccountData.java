package org.vmikhailov.java.cleanarchitecture.entities.account;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.vmikhailov.java.cleanarchitecture.entities.Money;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;

import java.util.Objects;
import java.util.function.Predicate;

public abstract class AccountData<T, M extends Money<?>> implements Predicate<Account<T, M>> {
    @Getter
    @NonNull
    private final Client<T> client;
    @Getter
    @Setter
    private M money;

    protected AccountData(M money, Client<T> client) {
        this.money = money;
        this.client = client;
    }

    /**
     * Check is account data are the same!
     *
     * @param otherData {@code AccountData} to check with
     * @return - {@code true} if this {@code AccountData} has same content as the {@param otherData}.
     */
    public boolean isSameData(AccountData<T, M> otherData) {
        if (otherData == null) {
            return false;
        }
        if (Objects.equals(this.client.getId(), otherData.client.getId())) {
            return Objects.equals(money, otherData.money);
        }
        return false;
    }
}
