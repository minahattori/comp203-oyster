package com.tfl.billing;

import com.oyster.*;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.math.BigDecimal;
import java.util.*;

public class TravelTracker implements ScanListener {

    //final variables for journey prices
    static final BigDecimal OFF_PEAK_JOURNEY_PRICE = new BigDecimal("1.60");
    static final BigDecimal OFF_PEAK_LONG_JOURNEY_PRICE = new BigDecimal("2.70");
    static final BigDecimal PEAK_LONG_JOURNEY_PRICE = new BigDecimal("3.80");
    static final BigDecimal PEAK_JOURNEY_PRICE = new BigDecimal("2.90");

    private final List<JourneyEvent> eventLog = new ArrayList<JourneyEvent>();
    private final Set<UUID> currentlyTravelling = new HashSet<UUID>();


    public void chargeAccounts() {
        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();

        List<Customer> customers = customerDatabase.getCustomers();
        for (Customer customer : customers) {
            totalJourneysFor(customer);
        }
    }

    private void totalJourneysFor(Customer customer) {

        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        //extracted method to fine journeys for a particular customer
        findJourneysFor(customer, customerJourneyEvents);

        //extracted method to add journeys to customerJourneyEvents List
        List<Journey> journeys = addJourneysToList(customerJourneyEvents);

        //extracted method to calculate the total charge given the list of journeys and if any were during peak hours
        BigDecimal customerTotal = calculateTotalCharge(journeys);
        BigDecimal roundedCustomerTotal = roundToNearestPenny(customerTotal);

        //Used adapter class to execute the original methods of PaymentsSystems
        // and also to store the values of the customers and their total charges
        PaymentsSystemAdapter.getInstance().charge(customer, journeys, roundedCustomerTotal);
    }

    private BigDecimal calculateTotalCharge(List<Journey> journeys) {
        //peak boolean used for daily caps to check if there has been at least one peak journey
        boolean peak = false;
        BigDecimal customerTotal = new BigDecimal(0);

        // calculates customers total for the day
        for (Journey journey : journeys) {
            if (peak(journey)){
                peak = true;
            }
            //extracted method to calculate one journey charge
            BigDecimal journeyPrice = calculateOneJourneyCharge(journey);
            customerTotal = customerTotal.add(journeyPrice);
        }

        //checks if there should be a daily cap on the customer's total charge
        if(peak && customerTotal.compareTo(new BigDecimal(9.00)) > 0) {
            customerTotal = new BigDecimal("9.00");
        } else if (customerTotal.compareTo(new BigDecimal(7.00)) > 0) {
            customerTotal = new BigDecimal("7.00");
        }
        return customerTotal;
    }

    //extracted method to calculate the charge of one journey
    private BigDecimal calculateOneJourneyCharge(Journey journey) {
        BigDecimal journeyPrice = OFF_PEAK_JOURNEY_PRICE;
        if (peak(journey)) {
            journeyPrice = PEAK_JOURNEY_PRICE;
            if (longJourney(journey)) {
                journeyPrice = PEAK_LONG_JOURNEY_PRICE;
            }
        } else if (longJourney(journey)) {
            journeyPrice = OFF_PEAK_LONG_JOURNEY_PRICE;
        }
        return journeyPrice;
    }

    //extracted method to keep methods clear in totalJourneysFor
    private List<Journey> addJourneysToList(List<JourneyEvent> customerJourneyEvents) {
        List<Journey> journeys = new ArrayList<Journey>();

        JourneyEvent start = null;
        for (JourneyEvent event : customerJourneyEvents) {
            //for every journey that a customer goes on, sets start to the event, and if the journey ended
            //adds the journey to the list of journeys
            if (event instanceof JourneyStart) {
                start = event;
            }
            if (event instanceof JourneyEnd && start != null) {
                journeys.add(new Journey(start, event));
                start = null;
            }
        }
        return journeys;
    }

    //extracted method to keep methods clear in totalJourneysFor
    private void findJourneysFor(Customer customer, List<JourneyEvent> customerJourneyEvents) {
        for (JourneyEvent journeyEvent : eventLog) {
            if (journeyEvent.cardId().equals(customer.cardId())) {
                customerJourneyEvents.add(journeyEvent);
            }
        }
    }


    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence) {
        return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private boolean longJourney(Journey journey) { return journey.durationSeconds() > (25*60) ;}

    private boolean peak(Journey journey) {
        return peak(journey.startTime()) || peak(journey.endTime());
    }

    private boolean peak(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 19);
    }

    public void connect(OysterCardReader... cardReaders) {
        for (OysterCardReader cardReader : cardReaders) {
            cardReader.register(this);
        }
    }

    @Override
    public void cardScanned(UUID cardId, UUID readerId) {
        JourneyEnd e1 = new JourneyEnd(cardId, readerId);
        JourneyStart e2 = new JourneyStart(cardId, readerId);

        cardScannedHelper(cardId, e1, e2);
    }

    //new cardScanned method to use when we want to test for a specific time
    protected void cardScanned(UUID cardId, UUID readerId, Date time) {
        long t = time.getTime();
        JourneyEnd e1 = new JourneyEnd(cardId, readerId, t);
        JourneyStart e2 = new JourneyStart(cardId, readerId, t);

        cardScannedHelper(cardId, e1, e2);
    }

    //extracted method in both cardScanned methods to eliminate repitition
    private void cardScannedHelper(UUID cardId, JourneyEnd e1, JourneyStart e2) {
        if (currentlyTravelling.contains(cardId)) {
            eventLog.add(e1);
            currentlyTravelling.remove(cardId);
        } else {
            if (CustomerDatabase.getInstance().isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                eventLog.add(e2);
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }
    }
}
