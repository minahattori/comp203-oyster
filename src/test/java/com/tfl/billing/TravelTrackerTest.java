package com.tfl.billing;
import com.tfl.billing.*;
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


// create our own customer database as current one is unreliable. (new adapter class new function so that it works 100%)
// adaptor or mock object is okay, up to your choice
// to create folder structure, unmark src as source direct, or use finder. mark folder above com.tfl.billing as test or src
// ok to change structure / go through adapter first.
//

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

    //List<Customer> custdata = CustomerDatabase.getInstance().getCustomers();
    CustomerDatabaseAdapter db = new CustomerDatabaseAdapter();
    PaymentsSystemAdapter psa = PaymentsSystemAdapter.getInstance();

    //List<JourneyEvent> eventlog = new ArrayList<JourneyEvent>();
    //travelTracker.connect(paddingtonReader,bakerStreetReader);


    @Test
    public void checkChargeAccountsWithShortTrip(){ //check customer is charged correctly for one journey

        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e555");
        Customer cust = new Customer ("Sherry Xu", o1);
        db.add(cust);

        //simulate a short off peak journey
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 15, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 15, 15));
        //assertThat(travelTracker.calculateChargeFor(cust), is(new BigDecimal(1.60)));
        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("1.60")));

    }

    @Test
    public void checkChargeAccountsWithLongTrip() {
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e556");
        Customer cust = new Customer ("Annie Chen", o1);
        db.add(cust);

        //simulate a long off peak journey
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 15, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 16, 30));
        travelTracker.chargeAccounts();

        //poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP)
        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("2.70")));

    }

    @Test
    public void checkDailyCapforOffPeakCustomer(){
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e557");
        Customer cust = new Customer ("Mina Hattori", o1);
        db.add(cust);

        //simulate multiple off peak journeys totalling over 7.00
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 12, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 13, 30));

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),new Date(2017, 11, 11, 13, 30));
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 14, 30));

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 14, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 15, 30));

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),new Date(2017, 11, 11, 15, 30));
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 16, 30));

        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("7.00")));


    }

    @Test
    public void checkDailyCapforPeakCustomer(){
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e559");
        Customer cust = new Customer ("Beyonce Knowless", o1);
        db.add(cust);

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 12, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 13, 30));

        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),new Date(2017, 11, 11, 13, 30));
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 14, 30));

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 14, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 15, 30));

        //include one peak journey
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(),new Date(2017, 11, 11, 18, 30));
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(), new Date(2017, 11, 11, 17, 30));

        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("9.00")));

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



/**
    @Test
    public void checkPeak() {
        Date d = new Date(2017, 11, 20, 18, 30);
        travelTracker.peak(d);
    } **/

}
