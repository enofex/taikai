package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Test;

class ProtectedMembersTest {

  @Test
  void shouldPassWhenFinalClassHasNoProtectedMembers() {
    Taikai taikai = Taikai.builder()
        .classes(ValidFinalClass.class)
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenFinalClassHasProtectedField() {
    Taikai taikai = Taikai.builder()
        .classes(FinalClassWithProtectedField.class)
        .java(JavaConfigurer::finalClassesShouldNotHaveProtectedMembers)
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldFailWhenFinalClassHasProtectedMethod() {
    Taikai taikai = Taikai.builder()
        .classes(FinalClassWithProtectedMethod.class)
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
