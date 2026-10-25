package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConstantNamingTest {

  @Test
  void shouldApplyConstantNamingConvention() {
    Taikai taikai = Taikai.builder()
        .classes(ValidConstants.class)
        .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenConstantDoesNotFollowConvention() {
    Taikai taikai = Taikai.builder()
        .classes(InvalidConstants.class)
        .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldIgnoreSerialVersionUID() {
    Taikai taikai = Taikai.builder()
        .classes(SerialVersionUIDClass.class)
        .java(java -> java.naming(NamingConfigurer::constantsShouldFollowConventions))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreExcludedFields() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerField.class)
        .java(
            java -> java.naming(naming -> naming.constantsShouldFollowConventions(List.of("log"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreExcludedFieldsIfMultipleFieldNamesAreProvided() {
    Taikai taikai = Taikai.builder()
        .classes(LoggerField.class)
        .java(
            java -> java.naming(
                naming -> naming.constantsShouldFollowConventions(List.of("log", "logger"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowOnSerialVersionUIDWhenNoFieldsAreExcluded() {
    Taikai taikai = Taikai.builder()
        .classes(SerialVersionUIDClass.class)
        .java(
            java -> java.naming(
                naming -> naming.constantsShouldFollowConventions(Collections.emptyList())))
        .build();

    assertThrows(AssertionError.class, taikai::check);
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

  static class LoggerField {

    private static final Logger log = LoggerFactory.getLogger(LoggerField.class);
  }
}
