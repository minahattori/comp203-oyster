package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.util.List;
import java.util.UUID;
import java.util.*;

public class CustomerDatabaseAdapter implements CustomerDatabaseInterface {

    CustomerDatabase db = CustomerDatabase.getInstance();

    public List<Customer> getCustomers() {
        return db.getCustomers();
    }

    public boolean add(Customer customer) {

        List<Customer> custList = getCustomers();
        custList.add(custList.size(), customer);

        return true; // always adding to list
    }

    public boolean isRegisteredId(UUID cardId) {
        return db.isRegisteredId(cardId);
    }


}




