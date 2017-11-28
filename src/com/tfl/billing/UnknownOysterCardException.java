package com.tfl.billing;

import java.util.UUID;

public class UnknownOysterCardException extends RuntimeException {
    public UnknownOysterCardException(UUID cardId) {
        super("Oyster Card does not correspond to a known customer. Id: " + cardId);
    }
}
