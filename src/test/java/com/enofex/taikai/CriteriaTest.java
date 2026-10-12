package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.api.Test;

class CriteriaTest {

  @Test
  void shouldMatchDirectAndComposedAnnotationsByClassAndName() {
    for (Class<?> type : new Class<?>[] {UserEntity.class, ComposedEntity.class}) {
      JavaClass imported = new ClassFileImporter().importClass(type);
      assertTrue(Criteria.annotatedWith(Entity.class).test(imported));
      assertTrue(Criteria.annotatedWith(Entity.class.getName()).test(imported));
    }
    JavaClass plain = new ClassFileImporter().importClass(UserDTO.class);
    assertFalse(Criteria.annotatedWith(Entity.class).test(plain));
  }

  @Test
  void shouldMatchEntireSimpleNameUsingRegex() {
    JavaClass imported = new ClassFileImporter().importClass(UserDTO.class);
    assertTrue(Criteria.namedLike(".*DTO").test(imported));
    assertFalse(Criteria.namedLike("DTO").test(imported));
    assertFalse(Criteria.namedLike("com.enofex.*").test(imported));
  }

  @Test
  void shouldMatchPackagesUsingArchUnitSyntax() {
    JavaClass imported = new ClassFileImporter().importClass(UserDTO.class);
    assertTrue(Criteria.inPackage("com.enofex.taikai").test(imported));
    assertTrue(Criteria.inPackage("com.enofex..").test(imported));
    assertTrue(Criteria.inPackage("com.*.taikai").test(imported));
    assertFalse(Criteria.inPackage("com.company.internal..").test(imported));
  }

  @Test
  void shouldComposeCriteriaWithAndAndOr() {
    JavaClass imported = new ClassFileImporter().importClass(UserEntity.class);
    assertTrue(Criteria.annotatedWith(Entity.class)
        .and(Criteria.inPackage("com.enofex.taikai")).test(imported));
    assertTrue(Criteria.namedLike(".*DTO").or(Criteria.annotatedWith(Entity.class)).test(imported));
    assertFalse(Criteria.namedLike(".*DTO").and(Criteria.annotatedWith(Entity.class)).test(imported));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Entity
  @Retention(RetentionPolicy.RUNTIME)
  @interface Composed {
  }

  @Entity
  static class UserEntity {
  }

  @Composed
  static class ComposedEntity {
  }

  static class UserDTO {
  }
}
