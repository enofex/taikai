package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassRecordTest {

  @Nested
  class ClassesAnnotatedWithShouldBeRecords {

    @Test
    void shouldNotThrowWhenAnnotatedClassesAreRecords_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ValidRecord.class)
          .java(java -> java.classesAnnotatedWithShouldBeRecords(TestAnnotation.class))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesAreNotRecords_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNonRecord.class)
          .java(java -> java.classesAnnotatedWithShouldBeRecords(TestAnnotation.class))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassesAreRecords_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(ValidRecord.class)
          .java(java -> java.classesAnnotatedWithShouldBeRecords(TestAnnotation.class.getName()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesAreNotRecords_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNonRecord.class)
          .java(java -> java.classesAnnotatedWithShouldBeRecords(TestAnnotation.class.getName()))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldBeRecords {

    @Test
    void shouldNotThrowWhenClassNameMatchesAndIsRecord() {
      Taikai taikai = Taikai.builder()
          .classes(ValidRecord.class)
          .java(java -> java.classesShouldBeRecords(".*ValidRecord"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNameMatchesAndIsNotRecord() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNonRecord.class)
          .java(java -> java.classesShouldBeRecords(".*InvalidNonRecord"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoClassNameMatches() {
      Taikai taikai = Taikai.builder()
          .classes(ValidRecord.class)
          .java(java -> java.classesShouldBeRecords(".*DoesNotExist"))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation { }

  @TestAnnotation
  record ValidRecord(String name, int id) { }

  @TestAnnotation
  static class InvalidNonRecord {
    private final String name = "test";
  }
}
