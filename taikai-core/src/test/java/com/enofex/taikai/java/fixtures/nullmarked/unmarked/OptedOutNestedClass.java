package com.enofex.taikai.java.fixtures.nullmarked.unmarked;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

@NullMarked
public class OptedOutNestedClass {

  @NullUnmarked
  static class Nested {

  }
}
