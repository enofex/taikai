package com.enofex.taikai.exports.metrics.domain;

public class Order {

  private final String id;
  private final LineItem item;

  public Order(String id) {
    this.id = id;
    this.item = new LineItem();
  }

  public String id() {
    return this.id;
  }
}
