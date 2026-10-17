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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FieldTypeTest {

  @ParameterizedTest
  @MethodSource("uuidSignatures")
  void shouldRejectForbiddenTypesAnywhereInFieldSignature(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(java -> java.fieldsShouldNotBeOfType(
            FieldCriteria.annotatedWith(Id.class), ClassCriteria.type(UUID.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains("id"));
    assertTrue(failure.getMessage().contains(UUID.class.getName()));
  }

  static Stream<Class<?>> uuidSignatures() {
    return Stream.of(DirectField.class, ArrayField.class, NestedField.class,
        WildcardField.class, TypeVariableField.class);
  }

  @Test
  void shouldAllowSelectedFieldsWithOtherTypes() {
    Taikai taikai = Taikai.builder()
        .classes(LongIdEntity.class)
        .java(java -> java.fieldsShouldNotBeOfType(
            FieldCriteria.annotatedWith(Id.class), ClassCriteria.type(UUID.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldSelectFieldsByAnnotationName() {
    Taikai taikai = Taikai.builder()
        .classes(LongIdEntity.class)
        .java(java -> java.fieldsShouldNotBeOfType(
            FieldCriteria.annotatedWith(Id.class.getName()), ClassCriteria.type(UUID.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldPassWithFailOnEmptyWhenNoSelectedFieldHasForbiddenType() {
    Taikai taikai = Taikai.builder()
        .classes(LongIdEntity.class)
        .failOnEmpty(true)
        .java(java -> java.fieldsShouldNotBeOfType(
            FieldCriteria.annotatedWith(Id.class), ClassCriteria.type(UUID.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldRejectSubtypesSelectedByAssignableTo() {
    Taikai taikai = Taikai.builder()
        .classes(LegacyDateEntity.class)
        .java(java -> java.fieldsShouldNotBeOfType(
            FieldCriteria.nameMatching("updatedAt"),
            ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class))))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(Timestamp.class.getName()));
  }

  @Test
  void shouldReportEachViolatingField() {
    Taikai taikai = Taikai.builder()
        .classes(LegacyDateEntity.class)
        .java(java -> java.fieldsShouldNotBeOfTypeForClass(
            ClassCriteria.annotatedWith(Entity.class),
            ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class))))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains("createdAt"));
    assertTrue(failure.getMessage().contains("updatedAt"));
    assertTrue(failure.getMessage().contains("history"));
    assertFalse(failure.getMessage().contains("deletedAt"));
  }

  @Test
  void shouldIgnoreFieldsOfUnselectedClasses() {
    Taikai taikai = Taikai.builder()
        .classes(LegacyDateEntity.class, LegacyDateHolder.class)
        .java(java -> java.fieldsShouldNotBeOfTypeForClass(
            ClassCriteria.simpleNameMatching("LegacyDateHolder"),
            ClassCriteria.type(UUID.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldAllowEntitiesWithModernDateTypes() {
    Taikai taikai = Taikai.builder()
        .classes(ModernDateEntity.class)
        .java(java -> java.fieldsShouldNotBeOfTypeForClass(
            ClassCriteria.annotatedWith(Entity.class),
            ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldRejectTypesSelectedByPackage() {
    Taikai taikai = Taikai.builder()
        .classes(LegacyDateEntity.class)
        .java(java -> java.fieldsShouldNotBeOfTypeForClass(
            ClassCriteria.annotatedWith(Entity.class), ClassCriteria.inPackage("java.sql")))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains("updatedAt"));
    assertFalse(failure.getMessage().contains("createdAt"));
  }

  @Test
  void shouldHonorConfigurationExclusions() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.fieldsShouldNotBeOfType(
            FieldCriteria.annotatedWith(Id.class), ClassCriteria.type(UUID.class),
            Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(
                DirectField.class.getName(), ArrayField.class.getName(),
                NestedField.class.getName(), WildcardField.class.getName(),
                TypeVariableField.class.getName()))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldHonorConfigurationExclusionsForClass() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.fieldsShouldNotBeOfTypeForClass(
            ClassCriteria.simpleNameMatching("LegacyDateEntity|ModernDateEntity"),
            ClassCriteria.type(Date.class),
            Configuration.of(Namespace.IMPORT.WITH_TESTS,
                List.of(LegacyDateEntity.class.getName()))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Id {
  }

  static class DirectField {
    @Id
    private UUID id;
  }

  static class ArrayField {
    @Id
    private UUID[] id;
  }

  static class NestedField {
    @Id
    private Optional<Map<String, List<UUID>>> id;
  }

  static class WildcardField {
    @Id
    private List<? extends UUID> id;
  }

  static class TypeVariableField<T extends UUID> {
    @Id
    private T id;
  }

  @Entity
  static class LongIdEntity {
    @Id
    private Long id;

    private UUID externalId;
  }

  @Entity
  static class LegacyDateEntity {
    private Date createdAt;
    private Timestamp updatedAt;
    private List<Date> history;
    private Instant deletedAt;
  }

  static class LegacyDateHolder {
    private Date createdAt;
  }

  @Entity
  static class ModernDateEntity {
    private Instant createdAt;
    private List<Instant> history;
  }
}
