package com.tfl.billing;

import java.util.UUID;

public class JourneyStart extends JourneyEvent {

    //new constructor created for testing purposes
    protected JourneyStart(UUID cardId, UUID readerId, long time ) {
        super(cardId, readerId, time);
    }

    //original constructor
    public JourneyStart(UUID cardId, UUID readerId) {
        super(cardId, readerId);
    }
}
