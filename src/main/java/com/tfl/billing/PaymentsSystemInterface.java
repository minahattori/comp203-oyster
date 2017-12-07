package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.PaymentsSystem;

import java.math.BigDecimal;
import java.util.List;

//interface for PaymentsSystemAdapter
public interface PaymentsSystemInterface {

    void charge(Customer customer, List<Journey> journeys, BigDecimal totalBill);
}
