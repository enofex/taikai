package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FieldModifierTest {

  @Nested
  class FieldsAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAllAnnotatedFieldsHaveRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicFinalFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldsLackRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonPublicFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllAnnotatedFieldsHaveRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicFinalFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldsLackRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonPublicFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class FieldsAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedFieldsDoNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonStaticFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldsHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedFieldsDoNotHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(NonStaticFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedFieldsHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(StaticFields.class))
          .java(java -> java.fieldsAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Test
  void shouldSupportEmptyModifierCollections() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(PublicFinalFields.class))
        .java(java -> java.fieldsAnnotatedWithShouldHaveModifiers(
            TestAnnotation.class, Set.of()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation { }

  static class PublicFinalFields {
    @TestAnnotation
    public final int id = 1;
  }

  static class NonPublicFields {
    @TestAnnotation
    int value = 0; // missing PUBLIC, FINAL
  }

  static class StaticFields {
    @TestAnnotation
    public static int counter = 0;
  }

  static class NonStaticFields {
    @TestAnnotation
    public int counter = 0;
  }
}
