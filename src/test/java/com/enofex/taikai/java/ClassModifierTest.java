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

class ClassModifierTest {

  @Nested
  class ClassesAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAllAnnotatedClassesHaveRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicFinalClass.class))
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesLackRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicNonFinalClass.class))
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllAnnotatedClassesHaveRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicFinalClass.class))
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesLackRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicNonFinalClass.class))
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedClassesDoNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicNonFinalClass.class))
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(AbstractClass.class))
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, EnumSet.of(JavaModifier.ABSTRACT)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassesDoNotHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(PublicNonFinalClass.class))
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(new ClassFileImporter().importClasses(AbstractClass.class))
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), EnumSet.of(JavaModifier.ABSTRACT)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Test
  void shouldSupportEmptyModifierCollections() {
    Taikai taikai = Taikai.builder()
        .classes(new ClassFileImporter().importClasses(PublicFinalClass.class))
        .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
            TestAnnotation.class, Set.of()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface TestAnnotation {}

  @TestAnnotation
  public final class PublicFinalClass {}

  @TestAnnotation
  public class PublicNonFinalClass {}

  @TestAnnotation
  abstract class AbstractClass {}
}
