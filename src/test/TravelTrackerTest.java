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

    List<Customer> custdata = CustomerDatabase.getInstance().getCustomers();




    //List<JourneyEvent> eventlog = new ArrayList<JourneyEvent>();



    @Test
    public void checkChargeAccountsWithShortTrip(){ //check customer is charged correctly for one journey

        Customer cust = custdata.get(1);
        //CustomerDatabase.getInstance().add(cust);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), cust.cardId(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), cust.cardId(), new Long("1511872916070"));

        Journey j = new Journey(jstartEvent, jendEvent);

        travelTracker.connect(paddingtonReader,bakerStreetReader);

        Date s = j.startTime();
        Date e = j.endTime();

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),s);
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), e);
        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(1.60)));

    }

    @Test
    public void checkChargeAccountsWithLongTrip() {
        Customer cust = custdata.get(4);
        //CustomerDatabase.getInstance().add(cust);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), cust.cardId(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), cust.cardId(), new Long("1511874816070"));

        Journey j = new Journey(jstartEvent, jendEvent);

        travelTracker.connect(paddingtonReader,bakerStreetReader);

        Date s = j.startTime();
        Date e = j.endTime();

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),s);
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), e);
        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(2.70)));

    }

    @Test
    public void checkDailyCapforOffPeakCustomer(){
        Customer cust = custdata.get(2);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), cust.cardId(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), cust.cardId(), new Long("1511874816070"));

        JourneyEvent jstartEvent1 = new JourneyStart(bakerStreetReader.id(), cust.cardId(), new Long("1511875816049"));
        JourneyEvent jendEvent1 = new JourneyEnd(paddingtonReader.id(), cust.cardId(), new Long("1511877816070"));

        JourneyEvent jstartEvent2 = new JourneyStart(paddingtonReader.id(), cust.cardId(), new Long("1511878816049"));
        JourneyEvent jendEvent2 = new JourneyEnd(bakerStreetReader.id(), cust.cardId(), new Long("1511881816070"));

        JourneyEvent jstartEvent3 = new JourneyStart(bakerStreetReader.id(), cust.cardId(), new Long("1511882816049"));
        JourneyEvent jendEvent3 = new JourneyEnd(paddingtonReader.id(), cust.cardId(), new Long("1511884816070"));

        Journey j = new Journey(jstartEvent, jendEvent);
        Journey j1 = new Journey(jstartEvent1, jendEvent1);
        Journey j2 = new Journey(jstartEvent2, jendEvent2);
        Journey j3 = new Journey(jstartEvent3, jendEvent3);

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),j.startTime());
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), j.endTime());

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),j1.startTime());
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), j1.endTime());

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),j2.startTime());
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), j2.endTime());

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),j3.startTime());
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), j3.endTime());

        assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(7.00)));


    }

    @Test
    public void checkDailyCapforPeakCustomer(){
        Customer cust = custdata.get(3);

        JourneyEvent jstartEvent = new JourneyStart(paddingtonReader.id(), cust.cardId(), new Long("1511872816049"));
        JourneyEvent jendEvent = new JourneyEnd(bakerStreetReader.id(), cust.cardId(), new Long("1511874816070"));

        JourneyEvent jstartEvent1 = new JourneyStart(bakerStreetReader.id(), cust.cardId(), new Long("1511875816049"));
        JourneyEvent jendEvent1 = new JourneyEnd(paddingtonReader.id(), cust.cardId(), new Long("1511877816070"));

        JourneyEvent jstartEvent2 = new JourneyStart(paddingtonReader.id(),cust.cardId(), new Long("1511878816049"));
        JourneyEvent jendEvent2 = new JourneyEnd(bakerStreetReader.id(), cust.cardId(), new Long("1511881816070"));

        JourneyEvent jstartEvent3 = new JourneyStart(bakerStreetReader.id(), cust.cardId(), new Long("1511890816049"));
        JourneyEvent jendEvent3 = new JourneyEnd(paddingtonReader.id(), cust.cardId(), new Long("1511892816070"));

        Journey j = new Journey(jstartEvent, jendEvent);
        Journey j1 = new Journey(jstartEvent1, jendEvent1);
        Journey j2 = new Journey(jstartEvent2, jendEvent2);
        Journey j3 = new Journey(jstartEvent3, jendEvent3);

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),j.startTime());
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), j.endTime());

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),j1.startTime());
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), j1.endTime());

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),j2.startTime());
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), j2.endTime());

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),j3.startTime());
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), j3.endTime());

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


    //make method to create journeys
    //refactored to make testing easier
    private Journey makeJourney(Date start, Date end, UUID cardID, OysterCardReader startStation, OysterCardReader endStation){
        JourneyEvent jstartEvent = new JourneyStart(startStation.id(), cardID, start.getTime());
        JourneyEvent jendEvent = new JourneyEnd(endStation.id(), cardID, end.getTime());

        return new Journey(jstartEvent, jendEvent);
    }

   /*  @Test
    public void testAddJourneyToList(){
        Customer c = custdata.get(2);
        //different customer
        Customer c2 = custdata.get(3);

        //customer c's journeys
        //Journey j1 = makeJourney(new Date(2017, 12, 1, 4, 30), new Date(2017, 12, 1, 4, 40), c.cardId(), paddingtonReader, bakerStreetReader);
        //Journey j2 = makeJourney(new Date(2017, 11, 2, 4, 30), new Date(2017, 11, 2, 4, 40), c.cardId(), paddingtonReader, bakerStreetReader);



        List<JourneyEvent> clist = new ArrayList<JourneyEvent>();
        clist.add(j1);

        List<Journey> jlist = new ArrayList<Journey>();

        jlist = travelTracker.addJourneyToList(clist);

        assertThat(clist.get(0), is(j1));

    } */




/**
    @Test
    public void checkPeak() {
        Date d = new Date(2017, 11, 20, 18, 30);
        travelTracker.peak(d);
    } **/

}
