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



public class TravelTrackerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    // Initializing oyster card read stations to use for testing
    OysterCardReader paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
    OysterCardReader bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
    OysterCardReader kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);

    TravelTracker travelTracker = new TravelTracker();

    // Using the adapter classes for customer database and payment system
    CustomerDatabaseAdapter db = new CustomerDatabaseAdapter();
    PaymentsSystemAdapter psa = PaymentsSystemAdapter.getInstance();


    // Test for off peak short trip
    @Test
    public void checkChargeAccountsWithOffPeakShortTrip(){ //check customer is charged correctly for one journey

        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e555");
        Customer cust = new Customer ("Kris Jenner", o1);
        db.add(cust);

        //simulate a short off peak journey
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 15, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 15, 35));
        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("1.60")));
    }

    // Test for off peak long trip
    @Test
    public void checkChargeAccountsWithOffPeakLongTrip() {
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e556");
        Customer cust = new Customer ("Kaitlyn Jenner", o1);
        db.add(cust);

        //simulate a long off peak journey
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 15, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 16, 30));
        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("2.70")));

    }

    // Test for peak time short trip
    @Test
    public void checkChargeAccountsWithPeakShortTrip(){

        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e515");
        Customer cust = new Customer ("Khloe Kardashian", o1);
        db.add(cust);

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 18, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 18, 45));
        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("2.90")));
    }

    // Test for peak time long trip
    @Test
    public void checkChargeAccountsWithPeakLongTrip() {
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e556");
        Customer cust = new Customer ("Kim Kardashian", o1);
        db.add(cust);

        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 18, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 19, 30));
        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("3.80")));

    }

    // Test daily cap for off peak customer
    @Test
    public void checkDailyCapForOffPeakCustomer(){
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e557");
        Customer cust = new Customer ("Kourtney Kardashian", o1);
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


    // Test daily cap for peak customer
    @Test
    public void checkDailyCapforPeakCustomer(){  //check that daily cap works for customer with at least one peak journey
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e559");
        Customer cust = new Customer ("Rob Kardashian", o1);
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


    // Test charges for multiple customers
    @Test
    public void checkChargeAccountsWithMultipleCardsAndTrips() {
        OysterCard o1 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e256");
        Customer cust = new Customer ("Kendall Jenner", o1);
        db.add(cust);

        OysterCard o2 = new OysterCard("335a03cb-2be6-4ed3-b83e-94858b43e356");
        Customer cust1 = new Customer ("Kylie Jenner", o2);
        db.add(cust1);

        //simulate a long peak journey
        travelTracker.cardScanned(cust.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 18, 30));
        travelTracker.cardScanned(cust.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 19, 30));
        travelTracker.chargeAccounts();

        //simulate a long off peak journey
        travelTracker.cardScanned(cust1.cardId(), paddingtonReader.id(),new Date(2017, 11, 11, 13, 30));
        travelTracker.cardScanned(cust1.cardId(), kingsCrossReader.id(), new Date(2017, 11, 11, 14, 30));
        travelTracker.chargeAccounts();

        //simulate a long off peak journey
        travelTracker.cardScanned(cust1.cardId(), kingsCrossReader.id(),new Date(2017, 11, 11, 14, 30));
        travelTracker.cardScanned(cust1.cardId(), bakerStreetReader.id(), new Date(2017, 11, 11, 15, 30));
        travelTracker.chargeAccounts();

        assertThat(psa.findChargeForCustomer(cust.cardId()), is(new BigDecimal("3.80")));
        assertThat(psa.findChargeForCustomer(cust1.cardId()), is(new BigDecimal("5.40")));
    }

    // Test unregistered oyster card throws exception
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
