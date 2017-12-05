package com.tfl.billing;


import com.oyster.*;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.*;

public class PaymentsSystemAdapter {
    private static PaymentsSystemAdapter instance = new PaymentsSystemAdapter();

    HashMap<UUID, BigDecimal> accounts = new HashMap<UUID, BigDecimal>();
    PaymentsSystem p = PaymentsSystem.getInstance();

    static PaymentsSystemAdapter getInstance(){ return instance;}


    public void charge(Customer customer, List<Journey> journeys, BigDecimal totalBill){
        //System.out.println("charging customer");
        accounts.put(customer.cardId(), totalBill);
        //if(accounts.containsValue(totalBill)){
        //    System.out.println(totalBill + "included");
        //}
        //System.out.println("customer added in psa" + totalBill);
        p.charge(customer, journeys, totalBill);
        return;
    }

    public BigDecimal findChargeForCustomer(UUID custId){
        if(accounts.containsKey(custId)) {
            return accounts.get(custId);
        }else{
            return new BigDecimal(-1.00);
        }
    }



    //return the total bill
    //charge
}
