package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.io.IOException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodExceptionRuleTest {

  @Nested
  class MethodsShouldNotDeclareGenericExceptions {

    @Test
    void shouldNotThrowWhenNoMethodsDeclareGenericExceptions() {
      Taikai taikai = Taikai.builder()
          .classes(NoGenericExceptions.class)
          .java(JavaConfigurer::methodsShouldNotDeclareGenericExceptions)
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsDeclareExceptionOrRuntimeException() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresGenericExceptions.class)
          .java(JavaConfigurer::methodsShouldNotDeclareGenericExceptions)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class MethodsShouldNotDeclareSpecificException {

    @Test
    void shouldNotThrowWhenMethodsDoNotDeclareGivenException_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(NoIOExceptionDeclared.class)
          .java(java -> java.methodsShouldNotDeclareException(".*", IOException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsDeclareGivenException_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresIOException.class)
          .java(java -> java.methodsShouldNotDeclareException(".*", IOException.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodsDoNotDeclareGivenException_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(NoIOExceptionDeclared.class)
          .java(java -> java.methodsShouldNotDeclareException(".*", IOException.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodsDeclareGivenException_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresIOException.class)
          .java(java -> java.methodsShouldNotDeclareException(".*", IOException.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(DeclaresIOException.class)
          .java(java -> java.methodsShouldNotDeclareException("nonExistingMethod", IOException.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class NoGenericExceptions {

    void safeMethod() {
    }

    void anotherSafeMethod() {
    }
  }

  static class DeclaresGenericExceptions {

    void methodThrowsException() throws Exception {
      throw new Exception("generic exception");
    }

    void methodThrowsRuntimeException() throws RuntimeException {
      throw new RuntimeException("runtime");
    }
  }

  static class DeclaresIOException {

    void riskyOperation() throws IOException {
      throw new IOException("I/O error");
    }

    void anotherRiskyOperation() throws IOException {
      throw new IOException("Another I/O error");
    }
  }

  static class NoIOExceptionDeclared {

    void safeOperation() {
    }

    void anotherSafeOperation() {
    }
  }
}
