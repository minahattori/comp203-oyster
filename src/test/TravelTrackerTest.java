package test;

import com.tfl.billing. *;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import org.junit.Assert;
import org.junit.Test;


import com.oyster.*;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.*;



public class TravelTrackerTest {
    OysterCard myCard = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
    OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
    OysterCardReader bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
    OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);
    //JourneyStart start = new JourneyStart(38400000-8cf0-11bd-b23e-10b96e4ef00d,paddingtonReader);
    //JourneyEnd end = new JourneyEnd(38400000-8cf0-11bd-b23e-10b96e4ef00d,bakerStreetReader);
    //Journey journey1 = new Journey(start, end);
    TravelTracker travelTracker = new TravelTracker();


    @Test
    public void checkPeakJourney(){
        Date d = new Date (2017,11,11,7,01);
        assert(travelTracker.peak(d));
    }

    @Test
    public void checkChargeAccounts(){
        travelTracker.connect(paddingtonReader,bakerStreetReader, kingsCrossReader);

    }


    //mock object for...? how can we see that the travel tracker actually connected?
    // go through the example code
    // how does oyster card class interact with travel tracker (ex in travel tracker cardScanned
    // uses cardid and readerid...cardid is from oyster class?)


}
