package com.enofex.taikai.java.fixtures.nullmarked.marked;

import org.jspecify.annotations.NullUnmarked;

public class OptedOutMethod {

  @NullUnmarked
  void localClass() {
    class Local {

    }
  }
}
