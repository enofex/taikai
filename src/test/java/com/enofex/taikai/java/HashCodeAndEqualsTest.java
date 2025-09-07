package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class HashCodeAndEqualsTest {

  @Test
  void shouldPassWhenClassImplementsBothEqualsAndHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ValidClass.class))
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenClassImplementsEqualsButNotHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(EqualsOnlyClass.class))
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenClassImplementsHashCodeButNotEquals() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(HashCodeOnlyClass.class))
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldPassWhenClassImplementsNeitherEqualsNorHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(PlainClass.class))
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class ValidClass {

    @Override
    public boolean equals(Object obj) {
      return this == obj;
    }

    @Override
    public int hashCode() {
      return 42;
    }
  }

  static class EqualsOnlyClass {

    @Override
    public boolean equals(Object obj) {
      return this == obj;
    }
  }

  static class HashCodeOnlyClass {

    @Override
    public int hashCode() {
      return 123;
    }
  }

  static class PlainClass {

    private String field;
  }
}
