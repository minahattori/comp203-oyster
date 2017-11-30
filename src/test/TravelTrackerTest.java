package test;

import com.tfl.billing. *;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.*;


import com.oyster.*;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.*;



public class TravelTrackerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    //JourneyEvent je = context.mock(JourneyEvent());

    OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
    OysterCardReader bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
    OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);
    //JourneyStart start = new JourneyStart(38400000-8cf0-11bd-b23e-10b96e4ef00d,paddingtonReader);
    //JourneyEnd end = new JourneyEnd(38400000-8cf0-11bd-b23e-10b96e4ef00d,bakerStreetReader);
    //Journey journey1 = new Journey(start, end);
    TravelTracker travelTracker = new TravelTracker();



    //List<JourneyEvent> eventlog = new ArrayList<JourneyEvent>();



    @Test
    public void checkChargeAccountsWithShortTrip(){ //check customer is charged correctly for one journey
        OysterCard myCard = new OysterCard("3b5a03cb-2be6-4ed3-b83e-94858b43e407");
        Customer cust = new Customer("Alex Hood", myCard);
        //CustomerDatabase.getInstance().add(cust);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), myCard.id(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), myCard.id(), new Long("1511872916070"));

        Journey j = new Journey(jstartEvent, jendEvent);

        travelTracker.connect(paddingtonReader,bakerStreetReader);

        Date s = j.startTime();
        Date e = j.endTime();

        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(),s);
        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(), e);
        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(1.60)));

    }

    @Test
    public void checkChargeAccountsWithLongTrip() {

        OysterCard myCard = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        Customer cust = new Customer("Mina Hattori", myCard);
        //CustomerDatabase.getInstance().add(cust);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), myCard.id(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), myCard.id(), new Long("1511874816070"));

        Journey j = new Journey(jstartEvent, jendEvent);

        travelTracker.connect(paddingtonReader,bakerStreetReader);

        Date s = j.startTime();
        Date e = j.endTime();

        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(),s);
        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(), e);
        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(2.70)));

    }

    @Test
    public void checkDailyCapforOffPeakCustomer(){
        OysterCard myCard = new OysterCard("3f1b3b55-f266-4426-ba1b-bcc506541866");
        Customer cust = new Customer("Annie Chen", myCard);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), myCard.id(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), myCard.id(), new Long("1511874816070"));

        JourneyEvent jstartEvent1 = new JourneyStart(bakerStreetReader.id(), myCard.id(), new Long("1511875816049"));
        JourneyEvent jendEvent1 = new JourneyEnd(paddingtonReader.id(), myCard.id(), new Long("1511877816070"));

        JourneyEvent jstartEvent2 = new JourneyStart(paddingtonReader.id(), myCard.id(), new Long("1511878816049"));
        JourneyEvent jendEvent2 = new JourneyEnd(bakerStreetReader.id(), myCard.id(), new Long("1511881816070"));

        JourneyEvent jstartEvent3 = new JourneyStart(bakerStreetReader.id(), myCard.id(), new Long("1511882816049"));
        JourneyEvent jendEvent3 = new JourneyEnd(paddingtonReader.id(), myCard.id(), new Long("1511884816070"));

        Journey j = new Journey(jstartEvent, jendEvent);
        Journey j1 = new Journey(jstartEvent1, jendEvent1);
        Journey j2 = new Journey(jstartEvent2, jendEvent2);
        Journey j3 = new Journey(jstartEvent3, jendEvent3);

        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(),j.startTime());
        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(), j.endTime());

        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(),j1.startTime());
        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(), j1.endTime());

        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(),j2.startTime());
        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(), j2.endTime());

        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(),j3.startTime());
        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(), j3.endTime());

        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(7.00)));


    }

    @Test
    public void checkDailyCapforPeakCustomer(){
        OysterCard myCard = new OysterCard("07b0bcb1-87df-447f-bf5c-d9961ab9d01e");
        Customer cust = new Customer("Sherry Xu", myCard);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), myCard.id(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), myCard.id(), new Long("1511874816070"));

        JourneyEvent jstartEvent1 = new JourneyStart(bakerStreetReader.id(), myCard.id(), new Long("1511875816049"));
        JourneyEvent jendEvent1 = new JourneyEnd(paddingtonReader.id(), myCard.id(), new Long("1511877816070"));

        JourneyEvent jstartEvent2 = new JourneyStart(paddingtonReader.id(), myCard.id(), new Long("1511878816049"));
        JourneyEvent jendEvent2 = new JourneyEnd(bakerStreetReader.id(), myCard.id(), new Long("1511881816070"));

        JourneyEvent jstartEvent3 = new JourneyStart(bakerStreetReader.id(), myCard.id(), new Long("1511890816049"));
        JourneyEvent jendEvent3 = new JourneyEnd(paddingtonReader.id(), myCard.id(), new Long("1511892816070"));

        Journey j = new Journey(jstartEvent, jendEvent);
        Journey j1 = new Journey(jstartEvent1, jendEvent1);
        Journey j2 = new Journey(jstartEvent2, jendEvent2);
        Journey j3 = new Journey(jstartEvent3, jendEvent3);

        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(),j.startTime());
        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(), j.endTime());

        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(),j1.startTime());
        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(), j1.endTime());

        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(),j2.startTime());
        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(), j2.endTime());

        travelTracker.cardScanned(myCard.id(), bakerStreetReader.id(),j3.startTime());
        travelTracker.cardScanned(myCard.id(), paddingtonReader.id(), j3.endTime());

        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(9.00)));

    }

    @Test
    public void testUnknownCardDoesNotScan(){
        OysterCard myCard = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e407");
        try{
            travelTracker.cardScanned(myCard.id(), bakerStreetReader.id());
            Assert.fail("Expected Exception not Found");
        }catch(UnknownOysterCardException e){
            System.out.println("Expected Exception Found");
            return;
        }
        Assert.fail("Incorrect Exception Found");
    }

}
