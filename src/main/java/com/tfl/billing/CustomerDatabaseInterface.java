package com.tfl.billing;

import com.tfl.external.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerDatabaseInterface {

    List<Customer> getCustomers();

    public boolean add(Customer customer);

    boolean isRegisteredId(UUID cardId);
}
