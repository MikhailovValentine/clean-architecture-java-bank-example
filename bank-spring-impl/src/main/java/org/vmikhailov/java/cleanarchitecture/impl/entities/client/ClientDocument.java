package org.vmikhailov.java.cleanarchitecture.impl.entities.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ClientDocument {
    private final String documentNumber;
    private final DocumentType documentType;
    private final String clientName;
    private final String clientSurname;

    public enum DocumentType {
        PASSPORT,
        DRIVING_LICENCE
    }
}
