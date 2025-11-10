package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NoUsageOfSystemOutOrErrTest {

  @Nested
  class NoUsageOfSystemOutOrErr {

    @Test
    void shouldThrowWhenClassUsesSystemOut() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(UsesSystemOut.class))
          .java(JavaConfigurer::noUsageOfSystemOutOrErr)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenClassUsesSystemErr() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(UsesSystemErr.class))
          .java(JavaConfigurer::noUsageOfSystemOutOrErr)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenClassUsesBothSystemOutAndErr() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(UsesSystemOutAndErr.class))
          .java(JavaConfigurer::noUsageOfSystemOutOrErr)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassDoesNotUseSystemOutOrErr() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(DoesNotUseSystemStreams.class))
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
