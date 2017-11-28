package com.tfl.billing;

import java.util.UUID;

public class JourneyStart extends JourneyEvent {
    public JourneyStart(UUID cardId, UUID readerId, long time ) {
        super(cardId, readerId, time);
    }
    //orig
    public JourneyStart(UUID cardId, UUID readerId) {
        super(cardId, readerId);
    }
}
