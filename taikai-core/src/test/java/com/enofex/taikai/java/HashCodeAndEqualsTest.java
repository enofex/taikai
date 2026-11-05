package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HashCodeAndEqualsTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {ValidClass.class, PlainClass.class, ValidRecord.class,
      PlainRecord.class})
  void shouldPassWhenClassImplementsBothOrNeitherEqualsAndHashCode(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {EqualsOnlyClass.class, HashCodeOnlyClass.class})
  void shouldFailWhenClassImplementsOnlyOneOfEqualsAndHashCode(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::classesShouldImplementHashCodeAndEquals)
        .build();

    assertThrows(AssertionError.class, taikai::check);
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
