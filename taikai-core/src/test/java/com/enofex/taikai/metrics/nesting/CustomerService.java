package com.enofex.taikai.metrics.nesting;

public class CustomerService {

  Customer create(String name) {
    return new Customer.Builder().name(name).build();
  }

  boolean isBlocked(Customer customer) {
    return customer.status() == Customer.Status.BLOCKED;
  }

  Runnable billing() {
    return new Runnable() {
      @Override
      public void run() {
        new Invoice("1");
      }
    };
  }
}
