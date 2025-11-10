package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodsAnnotatedWithShouldNotBeAnnotatedWithTest {

  @Nested
  class ClassBasedAPI {

    @Test
    void shouldThrowWhenMethodHasBothConflictingAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(ConflictingAnnotationsService.class))
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              PrimaryAnnotation.class, ConflictingAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodHasOnlyPrimaryAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(SafeAnnotatedService.class))
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              PrimaryAnnotation.class, ConflictingAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodHasOnlyConflictingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(OnlyConflictingService.class))
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              PrimaryAnnotation.class, ConflictingAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class StringBasedAPI {

    @Test
    void shouldThrowWhenMethodHasBothConflictingAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(ConflictingAnnotationsService.class))
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              PrimaryAnnotation.class.getName(), ConflictingAnnotation.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodHasOnlyPrimaryAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(SafeAnnotatedService.class))
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              PrimaryAnnotation.class.getName(), ConflictingAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodHasOnlyConflictingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(OnlyConflictingService.class))
          .java(java -> java.methodsAnnotatedWithShouldNotBeAnnotatedWith(
              PrimaryAnnotation.class.getName(), ConflictingAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface PrimaryAnnotation {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ConflictingAnnotation {
  }

  static class ConflictingAnnotationsService {

    @PrimaryAnnotation
    @ConflictingAnnotation
    void doSomething() {
    }
  }

  static class SafeAnnotatedService {

    @PrimaryAnnotation
    void doSomething() {
    }
  }

  static class OnlyConflictingService {

    @ConflictingAnnotation
    void doSomething() {
    }
  }
}
