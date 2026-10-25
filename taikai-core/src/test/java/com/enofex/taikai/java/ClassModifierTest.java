package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
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
  class ClassesShouldHaveModifiersByAnnotation {

    @Test
    void shouldNotThrowWhenAllAnnotatedClassesHaveRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesLackRequiredModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAllAnnotatedClassesHaveRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class.getName()), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesLackRequiredModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class.getName()), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotHaveModifiersByAnnotation {

    @Test
    void shouldNotThrowWhenAnnotatedClassesDoNotHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesHaveForbiddenModifiers_ClassVersion() {
      Taikai taikai = Taikai.builder()
          .classes(AbstractClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenAnnotatedClassesDoNotHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class.getName()), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassesHaveForbiddenModifiers_StringVersion() {
      Taikai taikai = Taikai.builder()
          .classes(AbstractClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.annotatedWith(TestAnnotation.class.getName()), List.of(JavaModifier.ABSTRACT)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ClassesShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenClassHasAllRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassMissingOneOrMoreRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicNonFinalClass"),
              List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*DoesNotMatch"),
              List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenModifiersListIsEmpty() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              List.of())) // empty list → always allowed
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              List.of(JavaModifier.PUBLIC, JavaModifier.FINAL),
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ClassesShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenClassDoesNotHaveDisallowedModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*PackagePrivateClass"),
              EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassHasAllDisallowedModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*PublicFinalClass"),
              EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*DoesNotMatch"),
              EnumSet.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Test
  void shouldSupportEmptyModifierCollections() {
    Taikai taikai = Taikai.builder()
        .classes(PublicFinalClass.class)
        .java(java -> java.classesShouldHaveModifiers(
            ClassCriteria.annotatedWith(TestAnnotation.class), Set.of()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldSupportConfigurationForClassesShouldHaveModifiersByAnnotation() {
    Taikai taikai = Taikai.builder()
        .classes(PublicFinalClass.class)
        .java(java -> java.classesShouldHaveModifiers(
            ClassCriteria.annotatedWith(TestAnnotation.class), List.of(JavaModifier.PUBLIC, JavaModifier.FINAL),
            com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldSupportConfigurationForClassesShouldNotHaveModifiersByAnnotation() {
    Taikai taikai = Taikai.builder()
        .classes(PublicNonFinalClass.class)
        .java(java -> java.classesShouldNotHaveModifiers(
            ClassCriteria.annotatedWith(TestAnnotation.class), List.of(JavaModifier.ABSTRACT),
            com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
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

  static class PackagePrivateClass {}
}
