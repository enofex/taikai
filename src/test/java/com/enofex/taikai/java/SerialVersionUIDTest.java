package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.io.Serializable;
import org.junit.jupiter.api.Test;

class SerialVersionUIDTest {

  @Test
  void shouldApplySerialVersionUIDNamingRule() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ValidSerialVersionUID.class))
        .java(JavaConfigurer::serialVersionUIDFieldsShouldBeStaticFinalLong)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldThrowWhenSerialVersionUIDIsNotStaticFinalLong() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(InvalidSerialVersionUID.class))
        .java(JavaConfigurer::serialVersionUIDFieldsShouldBeStaticFinalLong)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldIgnoreFieldsNotNamedSerialVersionUID() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ClassWithOtherConstant.class))
        .java(JavaConfigurer::serialVersionUIDFieldsShouldBeStaticFinalLong)
        .build();

    assertDoesNotThrow(taikai::check);
  }


  static class ValidSerialVersionUID implements Serializable {

    private static final long serialVersionUID = 1L;
  }

  static class InvalidSerialVersionUID implements Serializable {

    private static long serialVersionUID = 2L;
  }

  static class ClassWithOtherConstant {

    private static final long SOME_CONSTANT = 42L;
  }
}
