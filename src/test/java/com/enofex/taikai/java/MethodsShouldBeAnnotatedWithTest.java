package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodsShouldBeAnnotatedWithTest {

  @Nested
  class ClassBasedAPI {

    @Test
    void shouldNotThrowWhenMatchingMethodsAreAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith("process", TestAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingMethodsAreNotAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(NonAnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith("process", TestAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith("nonExistent", TestAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class StringBasedAPI {

    @Test
    void shouldNotThrowWhenMatchingMethodsAreAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(
              "process", TestAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingMethodsAreNotAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(NonAnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(
              "process", TestAnnotation.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenRegexDoesNotMatchAnyMethod() {
      Taikai taikai = Taikai.builder()
          .classes(AnnotatedService.class)
          .java(java -> java.methodsShouldBeAnnotatedWith(
              "nonExistent", TestAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation {
  }

  static class AnnotatedService {
    @TestAnnotation
    void process() {
      // annotated correctly
    }

    void otherMethod() {
      // ignored, name does not match
    }
  }

  static class NonAnnotatedService {
    void process() {
      // missing required annotation
    }
  }
}
