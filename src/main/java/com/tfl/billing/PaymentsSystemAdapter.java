package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.*;

public class PaymentsSystemAdapter implements PaymentsSystemInterface {

    private static PaymentsSystemAdapter instance = new PaymentsSystemAdapter();

    // accounts HashMap to store totalBills for each customer
    HashMap<UUID, BigDecimal> accounts = new HashMap<UUID, BigDecimal>();

    // retrieve instance of paymentsSystems
    PaymentsSystem p = PaymentsSystem.getInstance();

    static PaymentsSystemAdapter getInstance(){ return instance;}

    //adapted method to store the customer bill data into accounts hashmap
    // before calling paymentsSystem's charge method
    @Override
    public void charge(Customer customer, List<Journey> journeys, BigDecimal totalBill){
        accounts.put(customer.cardId(), totalBill);
        p.charge(customer, journeys, totalBill);
        return;
    }

    //method to return the totalBill of a customer, given their customerID
    public BigDecimal findChargeForCustomer(UUID custId){
        if(accounts.containsKey(custId)) {
            return accounts.get(custId);
        }else{
            return new BigDecimal(-1.00);
        }
    }

}
