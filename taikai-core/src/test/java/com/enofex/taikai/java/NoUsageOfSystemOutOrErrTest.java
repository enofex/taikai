package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NoUsageOfSystemOutOrErrTest {

  @Nested
  class NoUsageOfSystemOutOrErr {

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {UsesSystemOut.class, UsesSystemErr.class, UsesSystemOutAndErr.class})
    void shouldThrowWhenClassUsesSystemOutOrErr(Class<?> fixture) {
      Taikai taikai = Taikai.builder()
          .classes(fixture)
          .java(JavaConfigurer::noUsageOfSystemOutOrErr)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassDoesNotUseSystemOutOrErr() {
      Taikai taikai = Taikai.builder()
          .classes(DoesNotUseSystemStreams.class)
          .java(JavaConfigurer::noUsageOfSystemOutOrErr)
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class UsesSystemOut {
    void printSomething() {
      System.out.println("Hello, World!");
    }
  }

  static class UsesSystemErr {
    void logError() {
      System.err.println("An error occurred!");
    }
  }

  static class UsesSystemOutAndErr {
    void doSomething() {
      System.out.println("Working...");
      System.err.println("Something went wrong!");
    }
  }

  static class DoesNotUseSystemStreams {
    void logSafely() {
      // uses proper logging API instead of System.out/err
      String message = "safe logging";
      if (message.isEmpty()) {
        throw new IllegalStateException("Unexpected state");
      }
    }
  }
}
