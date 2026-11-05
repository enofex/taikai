package com.enofex.taikai.java;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.tngtech.archunit.core.domain.JavaModifier;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassModifierTest {

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
              defaultConfiguration()))
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

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateClass.class)
          .java(java -> java.classesShouldNotHaveModifiers(
              ClassCriteria.nameMatching(".*PackagePrivateClass"),
              EnumSet.of(JavaModifier.PUBLIC),
              defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  public final class PublicFinalClass {}

  public class PublicNonFinalClass {}

  static class PackagePrivateClass {}
}
