package com.tfl.billing;

import java.util.UUID;

public class JourneyEnd extends JourneyEvent {

    //new constructor created for testing purposes
    protected JourneyEnd(UUID cardId, UUID readerId, long time) {
        super(cardId, readerId, time);
    }

    //original constructor
    public JourneyEnd(UUID cardId, UUID readerId) {
        super(cardId, readerId);
    }
}


