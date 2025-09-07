package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

class ConstantNamingTest {

  @Test
  void shouldApplyConstantNamingConvention() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ValidConstants.class))
        .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenConstantDoesNotFollowConvention() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(InvalidConstants.class))
        .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldIgnoreSerialVersionUID() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(SerialVersionUIDClass.class))
        .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class ValidConstants {

    private static final String SOME_VALUE = "ABC";
    private static final int MAX_COUNT = 42;
  }

  static class InvalidConstants {

    private static final String someValue = "bad"; // not all caps
    private static final int MaxCount = 5;         // not fully uppercase
  }

  static class SerialVersionUIDClass implements Serializable {

    private static final long serialVersionUID = 1L;
  }
}
