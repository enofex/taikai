package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ProtectedMembersTest {

  @Test
  void shouldPassWhenFinalClassHasNoProtectedMembers() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(ValidFinalClass.class))
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenFinalClassHasProtectedField() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(FinalClassWithProtectedField.class))
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenFinalClassHasProtectedMethod() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(FinalClassWithProtectedMethod.class))
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }


  static final class ValidFinalClass {

    private int privateField;

    public void publicMethod() {
    }
  }

  static final class FinalClassWithProtectedField {

    protected String protectedField;
  }

  static final class FinalClassWithProtectedMethod {

    protected void protectedMethod() {
    }
  }
}
