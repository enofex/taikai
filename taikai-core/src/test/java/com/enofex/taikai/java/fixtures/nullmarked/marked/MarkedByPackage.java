package com.enofex.taikai.java.fixtures.nullmarked.marked;

public class MarkedByPackage {

  void anonymousClass() {
    new Object() {
    };
  }

  void localClass() {
    class Local {

    }
  }

  static class Nested {

  }
}
