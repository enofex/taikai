package com.enofex.taikai.java.nullmarked.marked;

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
