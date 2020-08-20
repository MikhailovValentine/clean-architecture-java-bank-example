package org.vmikhailov.java.cleanarchitecture.impl.entities.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.vmikhailov.java.cleanarchitecture.entities.client.Client;
import org.vmikhailov.java.cleanarchitecture.entities.client.ClientData;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Builder
@AllArgsConstructor
@Getter
public class UniversalBankClientData implements ClientData<Long> {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private List<ClientDocument> documents;

    @Override
    public boolean test(Client<Long> otherClient) {
        if (otherClient == null) {
            return false;
        }
        ClientData<Long> clientData = otherClient.getClientData();
        return this.equals(clientData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniversalBankClientData that = (UniversalBankClientData) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(surname, that.surname) &&
                Objects.equals(birthDate, that.birthDate) &&
                Objects.equals(documents, that.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, birthDate, documents);
    }
}
