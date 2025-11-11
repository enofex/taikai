package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class NoSystemOutOrErrTest {

  @Test
  void shouldApplyNoSystemOutOrErrRule() {
    Taikai taikai = Taikai.builder()
        .classes(ValidSystemUsage.class)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenSystemOutOrErrIsUsed() {
    Taikai taikai = Taikai.builder()
        .classes(InvalidSystemUsage.class)
        .java(JavaConfigurer::noUsageOfSystemOutOrErr)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  static class ValidSystemUsage {

    void logSomething() {
      String msg = "no system out or err here";
      msg.toUpperCase();
    }
  }

  static class InvalidSystemUsage {

    void logToConsole() {
      System.out.println("This should be forbidden");
    }

    void logError() {
      System.err.println("This should also be forbidden");
    }
  }
}
