package com.enofex.taikai.java.fixtures.nullmarked.unmarked;

import org.jspecify.annotations.NullMarked;

public class MarkedCodeUnits {

  @NullMarked
  public MarkedCodeUnits() {
    class InConstructor {

    }
  }

  @NullMarked
  void localClass() {
    class InMethod {

    }
  }
}
