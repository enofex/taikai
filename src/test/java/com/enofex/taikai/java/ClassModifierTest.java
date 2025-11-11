package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassModifierTest {

  @Nested
  class ClassesAnnotatedWithShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenAllAnnotatedClassesHaveRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesLackRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllAnnotatedClassesHaveRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesLackRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesAnnotatedWithShouldHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesAnnotatedWithShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenAnnotatedClassesDoNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(AbstractClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class, List.of(JavaModifier.ABSTRACT)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassesDoNotHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(AbstractClass.class)
          .java(java -> java.classesAnnotatedWithShouldNotHaveModifiers(
              TestAnnotation.class.getName(), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Test
  void shouldSupportEmptyModifierCollections() {
    Taikai taikai = Taikai.builder()
        .classes(PublicFinalClass.class)
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
