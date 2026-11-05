package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import java.io.Serializable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SerialVersionUIDTest {

  @Test
  void shouldApplySerialVersionUIDNamingRule() {
    Taikai taikai = Taikai.builder()
        .classes(ValidSerialVersionUID.class)
        .java(JavaConfigurer::serialVersionUIDFieldsShouldBeStaticFinalLong)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(classes = {InvalidSerialVersionUID.class, SerialVersionUIDWithStringType.class})
  void shouldThrowWhenSerialVersionUIDIsNotStaticFinalLong(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(JavaConfigurer::serialVersionUIDFieldsShouldBeStaticFinalLong)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  static class ValidSerialVersionUID implements Serializable {

    private static final long serialVersionUID = 1L;
  }

  static class InvalidSerialVersionUID implements Serializable {

    private static long serialVersionUID = 2L;
  }

  static class SerialVersionUIDWithStringType implements Serializable {

    private static final String serialVersionUID = "wrong";
  }
}
