package com.enofex.taikai.java.nullmarked.unmarked;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class MarkedClass {

  void localClass() {
    class Local {

    }
  }

  static class Nested {

    static class DeeplyNested {

    }
  }
}
