package com.enfoex.taikai.spring;


public final class SpringRules {

  private SpringRules() {
  }

  public static Configurations configurations() {
    return new Configurations();
  }

  public static Controllers controllers() {
    return new Controllers();
  }
}
