package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.FieldCriteria;
import com.enofex.taikai.Namespace;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassesShouldHaveFieldTest {

  @Test
  void shouldPassWhenClassDeclaresRequiredField() {
    Taikai taikai = Taikai.builder()
        .classes(VersionedEntity.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.annotatedWith(Version.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldFailWhenClassLacksRequiredField() {
    Taikai taikai = Taikai.builder()
        .classes(UnversionedEntity.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.annotatedWith(Version.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(UnversionedEntity.class.getName()));
    assertTrue(failure.getMessage().contains("does not have a field"));
  }

  @Test
  void shouldAcceptRequiredFieldInheritedFromSuperclass() {
    Taikai taikai = Taikai.builder()
        .classes(InheritingEntity.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.annotatedWith(Version.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldReportEachClassWithoutRequiredField() {
    Taikai taikai = Taikai.builder()
        .classes(VersionedEntity.class, UnversionedEntity.class, AnotherUnversionedEntity.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.annotatedWith(Version.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(UnversionedEntity.class.getName()));
    assertTrue(failure.getMessage().contains(AnotherUnversionedEntity.class.getName()));
    assertFalse(failure.getMessage().contains(VersionedEntity.class.getName()));
  }

  @Test
  void shouldIgnoreUnselectedClasses() {
    Taikai taikai = Taikai.builder()
        .classes(VersionedEntity.class, PlainClass.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class.getName()),
            FieldCriteria.annotatedWith(Version.class.getName())))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldRequireAllCombinedFieldCriteriaOnTheSameField() {
    Taikai taikai = Taikai.builder()
        .classes(VersionedEntity.class, InstantVersionedEntity.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class),
            FieldCriteria.annotatedWith(Version.class).and(FieldCriteria.ofType(Long.class))))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(InstantVersionedEntity.class.getName()));
    assertFalse(failure.getMessage().contains(VersionedEntity.class.getName()));
  }

  @Test
  void shouldSelectRequiredFieldByName() {
    Taikai taikai = Taikai.builder()
        .classes(VersionedEntity.class, UnversionedEntity.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.nameMatching("id")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldHonorConfigurationExclusions() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.annotatedWith(Version.class),
            Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(
                UnversionedEntity.class.getName(), AnotherUnversionedEntity.class.getName()))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldAllowNoMatchingClasses() {
    Taikai taikai = Taikai.builder()
        .classes(PlainClass.class)
        .java(java -> java.classesShouldHaveField(
            ClassCriteria.annotatedWith(Entity.class), FieldCriteria.annotatedWith(Version.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Version {
  }

  @Entity
  static class VersionedEntity {
    private Long id;

    @Version
    private Long version;
  }

  @Entity
  static class InstantVersionedEntity {
    private Long id;

    @Version
    private Instant version;
  }

  @Entity
  static class UnversionedEntity {
    private Long id;
  }

  @Entity
  static class AnotherUnversionedEntity {
    private Long id;
  }

  abstract static class VersionedBase {
    @Version
    private Long version;
  }

  @Entity
  static class InheritingEntity extends VersionedBase {
    private Long id;
  }

  static class PlainClass {
    private String name;
  }
}
