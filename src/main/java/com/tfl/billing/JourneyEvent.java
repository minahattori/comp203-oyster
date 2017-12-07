package com.tfl.billing;

import java.util.UUID;

public abstract class JourneyEvent {

    private final UUID cardId;
    private final UUID readerId;
    private final long time;

    //original constructor
    public JourneyEvent(UUID cardId, UUID readerId) {
        this.cardId = cardId;
        this.readerId = readerId;
        this.time = System.currentTimeMillis();
    }

    //new constructor created for testing purposes
    protected JourneyEvent(UUID cardId, UUID readerId, long time){
        this.cardId = cardId;
        this.readerId = readerId;
        //set given time instead of using the current time
        this.time = time;
    }

    public UUID cardId() {
        return cardId;
    }

    public UUID readerId() {
        return readerId;
    }

    public long time() {
        return time;
    }
}
