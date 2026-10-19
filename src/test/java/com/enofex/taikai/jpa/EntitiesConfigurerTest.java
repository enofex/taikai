package com.enofex.taikai.jpa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Namespace;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EntitiesConfigurerTest {

  @Nested
  class ShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenEntityIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldBePackagePrivate))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenEntityIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldBePackagePrivate))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(PublicEntity.class.getName()));
    }

    @Test
    void shouldIgnoreClassesThatAreNotEntities() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldBePackagePrivate))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class IdsShouldNotBeOfType {

    @Test
    void shouldNotThrowWhenIdHasAllowedTypeAndOtherFieldsHaveForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(LongIdEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenIdHasForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(UuidIdEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(UuidIdEntity.class.getName() + ".id"));
      assertTrue(failure.getMessage().contains(UUID.class.getName()));
    }

    @Test
    void shouldThrowWhenIdInheritedFromMappedSuperclassHasForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(UuidBaseEntity.class, InheritingUuidEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(UuidBaseEntity.class.getName() + ".id"));
    }

    @Test
    void shouldThrowWhenEmbeddedIdHasForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(EmbeddedIdEntity.class, OrderLineId.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .idsShouldNotBeOfType(ClassCriteria.type(OrderLineId.class))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(EmbeddedIdEntity.class.getName() + ".id"));
    }

    @Test
    void shouldIgnoreIdsInClassesThatAreNotEntities() {
      Taikai taikai = Taikai.builder()
          .classes(UuidIdHolder.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldHonorConfigurationExclusions() {
      Taikai taikai = Taikai.builder()
          .namespace("com.enofex.taikai.jpa")
          .jpa(jpa -> jpa.entities(entities -> entities
              .idsShouldNotBeOfType(ClassCriteria.type(UUID.class),
                  Configuration.of(Namespace.IMPORT.WITH_TESTS,
                      List.of(UuidIdEntity.class.getName(), UuidBaseEntity.class.getName())))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldHaveFieldAnnotatedWithVersion {

    @Test
    void shouldNotThrowWhenEntityDeclaresVersionField() {
      Taikai taikai = Taikai.builder()
          .classes(VersionedEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldHaveFieldAnnotatedWithVersion))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenVersionFieldIsInheritedFromMappedSuperclass() {
      Taikai taikai = Taikai.builder()
          .classes(InheritingVersionedEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldHaveFieldAnnotatedWithVersion))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenEntityHasNoVersionField() {
      Taikai taikai = Taikai.builder()
          .classes(LongIdEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldHaveFieldAnnotatedWithVersion))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(LongIdEntity.class.getName()));
      assertTrue(failure.getMessage().contains(Version.class.getName()));
    }

    @Test
    void shouldIgnoreClassesThatAreNotEntities() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldHaveFieldAnnotatedWithVersion))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FieldsShouldNotBeOfType {

    @Test
    void shouldNotThrowWhenEntityUsesAllowedTypes() {
      Taikai taikai = Taikai.builder()
          .classes(ModernDateEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .fieldsShouldNotBeOfType(legacyDateTypes())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowForEachFieldWithForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyDateEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .fieldsShouldNotBeOfType(legacyDateTypes())))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains("createdAt"));
      assertTrue(failure.getMessage().contains("updatedAt"));
      assertTrue(failure.getMessage().contains("history"));
      assertFalse(failure.getMessage().contains("deletedAt"));
    }

    @Test
    void shouldThrowWhenMappedSuperclassFieldHasForbiddenType() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyDateBase.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .fieldsShouldNotBeOfType(legacyDateTypes())))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(LegacyDateBase.class.getName() + ".createdAt"));
    }

    @Test
    void shouldIgnoreClassesThatAreNotEntities() {
      Taikai taikai = Taikai.builder()
          .classes(LegacyDateHolder.class)
          .jpa(jpa -> jpa.entities(entities -> entities
              .fieldsShouldNotBeOfType(legacyDateTypes())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldHonorConfigurationExclusions() {
      Taikai taikai = Taikai.builder()
          .namespace("com.enofex.taikai.jpa")
          .jpa(jpa -> jpa.entities(entities -> entities
              .fieldsShouldNotBeOfType(legacyDateTypes(),
                  Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(
                      LegacyDateEntity.class.getName(), LegacyDateBase.class.getName())))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenEntityNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities.namesShouldMatch(".+Entity")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenEntityNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(Customer.class)
          .jpa(jpa -> jpa.entities(entities -> entities.namesShouldMatch(".+Entity")))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(Customer.class.getName()));
    }

    @Test
    void shouldIgnoreClassesThatAreNotEntities() {
      Taikai taikai = Taikai.builder()
          .classes(PublicNonEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities.namesShouldMatch(".+Entity")))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableEntitiesConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .jpa(jpa -> jpa.entities(entities -> {
            entities.shouldBePackagePrivate();
            entities.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnSameConfigurerFromDisable() {
      Taikai.builder()
          .classes(PublicEntity.class)
          .jpa(jpa -> jpa.entities(entities -> assertSame(entities, entities.disable())))
          .build();
    }
  }

  private static DescribedPredicate<JavaClass> legacyDateTypes() {
    return ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class));
  }

  @Entity
  static class PackagePrivateEntity {
    @Id
    private Long id;
  }

  @Entity
  public static class PublicEntity {
    @Id
    private Long id;
  }

  public static class PublicNonEntity {
    private Date createdAt;
  }

  @Entity
  static class LongIdEntity {
    @Id
    private Long id;

    private UUID externalId;
  }

  @Entity
  static class UuidIdEntity {
    @Id
    private UUID id;
  }

  static class UuidIdHolder {
    @Id
    private UUID id;
  }

  @Embeddable
  static class OrderLineId {
    private Long orderId;
    private Integer line;
  }

  @Entity
  static class EmbeddedIdEntity {
    @EmbeddedId
    private OrderLineId id;
  }

  @MappedSuperclass
  abstract static class UuidBaseEntity {
    @Id
    private UUID id;
  }

  @Entity
  static class InheritingUuidEntity extends UuidBaseEntity {
    private String name;
  }

  @Entity
  static class VersionedEntity {
    @Id
    private Long id;

    @Version
    private Long version;
  }

  @MappedSuperclass
  abstract static class VersionedBase {
    @Version
    private Long version;
  }

  @Entity
  static class InheritingVersionedEntity extends VersionedBase {
    @Id
    private Long id;
  }

  @Entity
  static class LegacyDateEntity {
    @Id
    private Long id;

    private Date createdAt;
    private Timestamp updatedAt;
    private List<Date> history;
    private Instant deletedAt;
  }

  @MappedSuperclass
  abstract static class LegacyDateBase {
    private Date createdAt;
  }

  static class LegacyDateHolder {
    private Date createdAt;
  }

  @Entity
  static class ModernDateEntity {
    @Id
    private Long id;

    private Instant createdAt;
    private List<Instant> history;
  }

  @Entity
  static class Customer {
    @Id
    private Long id;
  }
}
