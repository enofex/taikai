package com.enofex.taikai.java.fixtures.nullmarked.marked;

import org.jspecify.annotations.NullUnmarked;

public class OptedOutConstructor {

  @NullUnmarked
  public OptedOutConstructor() {
    class Local {

    }
  }
}
