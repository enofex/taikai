package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class HashCodeAndEqualsTest {

  @Test
  void shouldPassWhenClassImplementsBothEqualsAndHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(ValidClass.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenClassImplementsEqualsButNotHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(EqualsOnlyClass.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenClassImplementsHashCodeButNotEquals() {
    Taikai taikai = Taikai.builder()
        .classes(HashCodeOnlyClass.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldPassWhenClassImplementsNeitherEqualsNorHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(PlainClass.class)
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

  @Test
  void shouldPassWhenRecordImplementsNeitherEqualsNorHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(PlainRecord.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldPassWhenRecordImplementsBothEqualsAndHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(ValidRecord.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenRecordImplementsEqualsButNotHashCode() {
    Taikai taikai = Taikai.builder()
        .classes(EqualsOnlyRecord.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("implements equals() but not hashCode()"),
        error.getMessage());
  }

  @Test
  void shouldFailWhenRecordImplementsHashCodeButNotEquals() {
    Taikai taikai = Taikai.builder()
        .classes(HashCodeOnlyRecord.class)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("implements hashCode() but not equals()"),
        error.getMessage());
  }

  record PlainRecord(String value) {

  }

  record ValidRecord(String value) {

    @Override
    public boolean equals(Object o) {
      return o instanceof ValidRecord other && this.value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
      return this.value.toLowerCase().hashCode();
    }
  }

  record EqualsOnlyRecord(String value) {

    @Override
    public boolean equals(Object o) {
      return o instanceof EqualsOnlyRecord other && this.value.equalsIgnoreCase(other.value);
    }
  }

  record HashCodeOnlyRecord(String value) {

    @Override
    public int hashCode() {
      return this.value.toLowerCase().hashCode();
    }
  }

  static class PlainClass {

    private String field;
  }
}
