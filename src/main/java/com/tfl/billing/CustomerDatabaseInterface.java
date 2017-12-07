package com.tfl.billing;

import com.tfl.external.Customer;

import java.util.List;
import java.util.UUID;

//interface for CustomerDatabaseAdapter
public interface CustomerDatabaseInterface {

    List<Customer> getCustomers();

    boolean add(Customer customer);

    boolean isRegisteredId(UUID cardId);
}
