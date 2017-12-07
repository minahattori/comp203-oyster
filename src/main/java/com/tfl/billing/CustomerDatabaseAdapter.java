package com.tfl.billing;

import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;

import java.util.List;
import java.util.UUID;

public class CustomerDatabaseAdapter implements CustomerDatabaseInterface {

    CustomerDatabase db = CustomerDatabase.getInstance();

    // uses instance of CustomerDatabase to implement methods.
    public List<Customer> getCustomers() {
        return db.getCustomers();
    }

    // modified add function that allows us to add a customer 100% of the time
    public boolean add(Customer customer) {
        List<Customer> custList = getCustomers();
        custList.add(custList.size(), customer);
        return true; // always adding to list
    }

    //uses instance of CustomerDatabase to implement methods.
    public boolean isRegisteredId(UUID cardId) {
        return db.isRegisteredId(cardId);
    }


}




