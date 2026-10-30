package com.enofex.taikai.metrics.nesting;

public record Customer(String name, Status status) {

  public enum Status {
    ACTIVE, BLOCKED
  }

  public static final class Builder {

    private String name;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Customer build() {
      return new Customer(this.name, Status.ACTIVE);
    }
  }
}
