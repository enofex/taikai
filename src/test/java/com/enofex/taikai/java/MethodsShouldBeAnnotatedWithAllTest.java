package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodsShouldBeAnnotatedWithAllTest {

  @Nested
  class ClassBasedAPI {

    @Test
    void shouldNotThrowWhenMethodHasAllRequiredAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(FullyAnnotatedService.class))
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              BaseAnnotation.class, List.of(RequiredA.class, RequiredB.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsMissingOneRequiredAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PartiallyAnnotatedService.class))
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              BaseAnnotation.class, List.of(RequiredA.class, RequiredB.class)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodIsNotAnnotatedWithBaseAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonBaseAnnotatedService.class))
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              BaseAnnotation.class, List.of(RequiredA.class, RequiredB.class)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class StringBasedAPI {

    @Test
    void shouldNotThrowWhenMethodHasAllRequiredAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(FullyAnnotatedService.class))
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              BaseAnnotation.class.getName(),
              List.of(RequiredA.class.getName(), RequiredB.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodIsMissingOneRequiredAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PartiallyAnnotatedService.class))
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              BaseAnnotation.class.getName(),
              List.of(RequiredA.class.getName(), RequiredB.class.getName())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMethodIsNotAnnotatedWithBaseAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonBaseAnnotatedService.class))
          .java(java -> java.methodsShouldBeAnnotatedWithAll(
              BaseAnnotation.class.getName(),
              List.of(RequiredA.class.getName(), RequiredB.class.getName())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface BaseAnnotation {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface RequiredA {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface RequiredB {
  }

  static class FullyAnnotatedService {

    @BaseAnnotation
    @RequiredA
    @RequiredB
    void process() {
    }
  }

  static class PartiallyAnnotatedService {

    @BaseAnnotation
    @RequiredA
    void process() {
    }
  }

  static class NonBaseAnnotatedService {

    void process() {
      // No base annotation, should be ignored
    }
  }
}
