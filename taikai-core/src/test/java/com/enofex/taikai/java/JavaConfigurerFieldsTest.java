package com.enofex.taikai.java;

import static com.enofex.taikai.core.TaikaiRule.Configuration.defaultConfiguration;
import static com.enofex.taikai.java.JavaChecks.assertReportsViolations;
import static com.enofex.taikai.java.JavaChecks.imported;
import static com.enofex.taikai.java.JavaChecks.overload;
import static com.enofex.taikai.java.JavaFixtures.MARKED;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.core.FieldCriteria;
import com.enofex.taikai.core.TaikaiRule.Configuration;
import com.enofex.taikai.java.JavaFixtures.Entity;
import com.enofex.taikai.java.JavaFixtures.Marker;
import com.enofex.taikai.java.JavaFixtures.PackagePrivateField;
import com.enofex.taikai.java.JavaFixtures.PrivateFinalFields;
import com.enofex.taikai.java.JavaFixtures.StaticField;
import com.enofex.taikai.java.JavaFixtures.TestAnnotation;
import com.enofex.taikai.java.JavaFixtures.TypedFields;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JavaConfigurerFieldsTest {

  @Nested
  class FieldsShouldNotBePublic {

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("fieldsShouldNotBePublic()",
              JavaConfigurer::fieldsShouldNotBePublic,
              StaticFields.class, NonStaticFields.class),
          overload("fieldsShouldNotBePublic(Configuration)",
              java -> java.fieldsShouldNotBePublic(defaultConfiguration()),
              StaticFields.class, NonStaticFields.class));
    }
  }

  @Nested
  class FieldsShouldHaveModifiers {

    @Test
    void shouldNotThrowWhenFieldMatchingRegexHasRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalFields.class)
          .java(java -> java.fieldsShouldHaveModifiers(FieldCriteria.nameMatching("id"),
              List.of(JavaModifier.PUBLIC, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldMatchingRegexLacksRequiredModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(NonPublicFields.class)
          .java(java -> java.fieldsShouldHaveModifiers(FieldCriteria.nameMatching("value"),
              List.of(JavaModifier.PUBLIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenFieldNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(NonPublicFields.class)
          .java(java -> java.fieldsShouldHaveModifiers(FieldCriteria.nameMatching("nonexistent"),
              List.of(JavaModifier.PUBLIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportEmptyModifierCollections() {
      Taikai taikai = Taikai.builder()
          .classes(PublicFinalFields.class)
          .java(java -> java.fieldsShouldHaveModifiers(
              FieldCriteria.annotatedWith(TestAnnotation.class), Set.of()))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSelectFieldsByType() {
      Taikai taikai = Taikai.builder()
          .classes(TypedFields.class)
          .java(java -> java.fieldsShouldHaveModifiers(FieldCriteria.ofType(Date.class),
              List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("Fields of type java.util.Date"));
      assertTrue(failure.getMessage().contains("mutableDate"));
      assertFalse(failure.getMessage().contains(".name"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("fieldsShouldHaveModifiers(DescribedPredicate, Collection, Configuration)",
              java -> java.fieldsShouldHaveModifiers(FieldCriteria.nameMatching("name"),
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL), defaultConfiguration()),
              PrivateFinalFields.class, PackagePrivateField.class));
    }
  }

  @Nested
  class FieldsShouldHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, PackagePrivateField.class)
          .java(java -> java.fieldsShouldHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PrivateFinalFields"),
              List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, PackagePrivateField.class)
          .java(java -> java.fieldsShouldHaveModifiersForClass(MARKED,
              List.of(JavaModifier.PRIVATE, JavaModifier.FINAL)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(PackagePrivateField.class.getName()));
    }

    @Test
    void shouldIgnoreSyntheticFields() {
      assertTrue(new ClassFileImporter().importClass(PrivateFinalFields.class).getFields().stream()
          .anyMatch(field -> field.getModifiers().contains(JavaModifier.SYNTHETIC)));

      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class)
          .java(java -> java.fieldsShouldHaveModifiersForClass(
              MARKED, List.of(JavaModifier.PRIVATE)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("fieldsShouldHaveModifiersForClass(DescribedPredicate, Collection, "
                  + "Configuration)",
              java -> java.fieldsShouldHaveModifiersForClass(MARKED,
                  List.of(JavaModifier.PRIVATE, JavaModifier.FINAL), defaultConfiguration()),
              PrivateFinalFields.class, PackagePrivateField.class));
    }
  }

  @Nested
  class FieldsShouldNotHaveModifiers {

    @Test
    void shouldNotThrowWhenFieldMatchingRegexDoesNotHaveForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(NonStaticFields.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(FieldCriteria.nameMatching("counter"),
              List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldMatchingRegexHasForbiddenModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(StaticFields.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(FieldCriteria.nameMatching("counter"),
              List.of(JavaModifier.STATIC)))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenFieldNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(StaticFields.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(
              FieldCriteria.nameMatching("nonexistent"), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSelectFieldsByAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(TypedFields.class)
          .java(java -> java.fieldsShouldNotHaveModifiers(
              FieldCriteria.annotatedWith(Marker.class), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("overloads")
    void shouldReportViolationsWithEveryOverload(Customizer<JavaConfigurer> rule,
        Class<?> compliant, Class<?> violating) {
      assertReportsViolations(rule, compliant, violating);
    }

    static Stream<Arguments> overloads() {
      return Stream.of(
          overload("fieldsShouldNotHaveModifiers(DescribedPredicate, Collection, Configuration)",
              java -> java.fieldsShouldNotHaveModifiers(FieldCriteria.nameMatching("name|counter"),
                  List.of(JavaModifier.STATIC), defaultConfiguration()),
              PrivateFinalFields.class, StaticField.class));
    }
  }

  @Nested
  class FieldsShouldNotHaveModifiersForClass {

    @Test
    void shouldNotThrowWhenFieldsOfSelectedClassesLackModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(PrivateFinalFields.class, StaticField.class)
          .java(java -> java.fieldsShouldNotHaveModifiersForClass(
              ClassCriteria.simpleNameMatching("PrivateFinalFields"), List.of(JavaModifier.STATIC)))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenFieldsOfSelectedClassesHaveModifiers() {
      Taikai taikai = Taikai.builder()
          .classes(StaticField.class)
          .java(java -> java.fieldsShouldNotHaveModifiersForClass(
              MARKED, List.of(JavaModifier.STATIC)))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("counter"));
    }

    @Test
    void shouldHonorConfiguredClasses() {
      Taikai taikai = Taikai.builder()
          .classes(StaticField.class)
          .java(java -> java.fieldsShouldNotHaveModifiersForClass(
              MARKED, List.of(JavaModifier.STATIC),
              Configuration.of(imported(PrivateFinalFields.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldNotBeOfType {

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
    void shouldHonorConfigurationExclusions() {
      Taikai taikai = Taikai.builder()
          .java(java -> java.fieldsShouldNotBeOfType(
              FieldCriteria.annotatedWith(Id.class), ClassCriteria.type(UUID.class),
              Configuration.of(
                  imported(LongIdEntity.class, DirectField.class, ArrayField.class,
                      NestedField.class, WildcardField.class, TypeVariableField.class),
                  List.of(DirectField.class.getName(), ArrayField.class.getName(),
                      NestedField.class.getName(), WildcardField.class.getName(),
                      TypeVariableField.class.getName()))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldNotBeOfTypeForClass {

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
    void shouldHonorConfigurationExclusionsForClass() {
      Taikai taikai = Taikai.builder()
          .java(java -> java.fieldsShouldNotBeOfTypeForClass(
              ClassCriteria.simpleNameMatching("LegacyDateEntity|ModernDateEntity"),
              ClassCriteria.type(Date.class),
              Configuration.of(imported(LegacyDateEntity.class, ModernDateEntity.class),
                  List.of(LegacyDateEntity.class.getName()))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class PublicFinalFields {
    @TestAnnotation
    public final int id = 1;
  }

  static class NonPublicFields {
    @TestAnnotation
    int value = 0; // missing PUBLIC, JavaModifier.FINAL
  }

  static class StaticFields {
    @TestAnnotation
    public static int counter = 0;
  }

  static class NonStaticFields {
    @TestAnnotation
    public int counter = 0;
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
