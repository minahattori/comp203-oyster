package com.tfl.billing;

import com.oyster.*;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.*;

public class TravelTracker implements ScanListener {

    static final BigDecimal OFF_PEAK_JOURNEY_PRICE = new BigDecimal(1.60);
    static final BigDecimal OFF_PEAK_LONG_JOURNEY_PRICE = new BigDecimal(2.70);
    static final BigDecimal PEAK_LONG_JOURNEY_PRICE = new BigDecimal(3.80);
    static final BigDecimal PEAK_JOURNEY_PRICE = new BigDecimal(2.90);

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
        boolean peak = false;
        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        // finds journeys for this customer
        findJourneysFor(customer, customerJourneyEvents);

        List<Journey> journeys = addJourneysToList(customerJourneyEvents);

        BigDecimal customerTotal = calculateTotalCharge(peak, journeys);
        PaymentsSystem.getInstance().charge(customer, journeys, roundToNearestPenny(customerTotal));

    }

    private BigDecimal calculateTotalCharge(boolean peak, List<Journey> journeys) {
        BigDecimal customerTotal = new BigDecimal(0);
        // calculates customers total for the day
        for (Journey journey : journeys) {
            BigDecimal journeyPrice = OFF_PEAK_JOURNEY_PRICE;
            if (peak(journey)) {
                journeyPrice = PEAK_JOURNEY_PRICE;
                peak = true;
                if (longJourney(journey)) {
                    journeyPrice = PEAK_LONG_JOURNEY_PRICE;
                }
            } else if (longJourney(journey)) {
                journeyPrice = OFF_PEAK_LONG_JOURNEY_PRICE;
            }
            customerTotal = customerTotal.add(journeyPrice);
        }


        if(peak && customerTotal.compareTo(new BigDecimal(9.00)) > 0) {
            customerTotal = new BigDecimal(9.00);
            peak = false;
        } else if (customerTotal.compareTo(new BigDecimal(7.00)) > 0) {
            customerTotal = new BigDecimal(7.00);
        }
        return customerTotal;
    }

    //extracted method to keep methods clear
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

    //extracted method to keep methods clear
    private void findJourneysFor(Customer customer, List<JourneyEvent> customerJourneyEvents) {
        for (JourneyEvent journeyEvent : eventLog) {
            if (journeyEvent.cardId().equals(customer.cardId())) {
                customerJourneyEvents.add(journeyEvent);
            }
        }
    }

    //copy of totalJourneysFor for testing
    public BigDecimal calculateChargeFor(Customer customer) {
        boolean peak = false;
        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        // finds journeys for this customer
        findJourneysFor(customer, customerJourneyEvents);

        List<Journey> journeys = addJourneysToList(customerJourneyEvents);

        BigDecimal customerTotal = calculateTotalCharge(peak, journeys);

        PaymentsSystem.getInstance().charge(customer, journeys, roundToNearestPenny(customerTotal));
        return customerTotal;
    }


    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence) {
        return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private boolean longJourney(Journey journey) { return journey.durationSeconds() > (25*60) ;}
    private boolean peak(Journey journey) {
        return peak(journey.startTime()) || peak(journey.endTime());
    }



    //CHANGE BACK TO PRIVATE!!!!!!!!!!!
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


    public void cardScannedTime(UUID cardId, UUID readerId, Date time) {

    }
    @Override
    public void cardScanned(UUID cardId, UUID readerId) {
        //long temp = new Long("1511872816049");
        if (currentlyTravelling.contains(cardId)) {
            JourneyEnd e1 = new JourneyEnd(cardId, readerId);
            //System.out.println("end" + e1.time());
            eventLog.add(e1);
            currentlyTravelling.remove(cardId);
        } else {
            if (CustomerDatabase.getInstance().isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                JourneyStart e2 = new JourneyStart(cardId, readerId);
                //System.out.println("start" + e2.time());
                eventLog.add(e2);
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }


    }

    // new
    public void cardScanned(UUID cardId, UUID readerId, Date time) {
        //long temp = new Long("1511872816049");
        long t = time.getTime();
        if (currentlyTravelling.contains(cardId)) {
            JourneyEnd e1 = new JourneyEnd(cardId, readerId, t);
            //System.out.println("end" + e1.time());
            eventLog.add(e1);
            currentlyTravelling.remove(cardId);
        } else {
            if (CustomerDatabase.getInstance().isRegisteredId(cardId)) {
                currentlyTravelling.add(cardId);
                JourneyStart e2 = new JourneyStart(cardId, readerId, t);
                //System.out.println("start" + e2.time());
                eventLog.add(e2);
            } else {
                throw new UnknownOysterCardException(cardId);
            }
        }


    }

}
