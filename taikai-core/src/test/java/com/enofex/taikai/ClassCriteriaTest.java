package com.enofex.taikai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassCriteriaTest {

  @Test
  void shouldMatchDirectAndComposedAnnotationsByClassAndName() {
    for (Class<?> type : new Class<?>[] {UserEntity.class, ComposedEntity.class}) {
      JavaClass imported = new ClassFileImporter().importClass(type);
      assertTrue(ClassCriteria.annotatedWith(Entity.class).test(imported));
      assertTrue(ClassCriteria.annotatedWith(Entity.class.getName()).test(imported));
    }
    JavaClass plain = new ClassFileImporter().importClass(UserDTO.class);
    assertFalse(ClassCriteria.annotatedWith(Entity.class).test(plain));
  }

  @Test
  void shouldMatchOnlyAnnotationsRetainedInClassFiles() {
    JavaClass imported = new ClassFileImporter().importClass(RetentionFixture.class);
    assertTrue(ClassCriteria.annotatedWith(ClassRetained.class).test(imported));
    assertFalse(ClassCriteria.annotatedWith(SourceRetained.class).test(imported));
  }

  @Test
  void shouldMatchEntireFullyQualifiedNameUsingRegex() {
    JavaClass imported = new ClassFileImporter().importClass(UserDTO.class);
    assertTrue(ClassCriteria.nameMatching(".*DTO").test(imported));
    assertTrue(ClassCriteria.nameMatching("com\\.enofex\\..*").test(imported));
    assertFalse(ClassCriteria.nameMatching("UserDTO").test(imported));
    assertFalse(ClassCriteria.nameMatching("DTO").test(imported));
  }

  @Test
  void shouldMatchEntireSimpleNameUsingRegex() {
    JavaClass imported = new ClassFileImporter().importClass(UserDTO.class);
    assertTrue(ClassCriteria.simpleNameMatching("UserDTO").test(imported));
    assertTrue(ClassCriteria.simpleNameMatching(".*DTO").test(imported));
    assertFalse(ClassCriteria.simpleNameMatching("DTO").test(imported));
    assertFalse(ClassCriteria.simpleNameMatching("com.enofex.*").test(imported));
  }

  @Test
  void shouldDescribeNameCriteria() {
    assertEquals("with names matching .*DTO", ClassCriteria.nameMatching(".*DTO").getDescription());
    assertEquals("with simple names matching UserDTO",
        ClassCriteria.simpleNameMatching("UserDTO").getDescription());
    assertEquals("in package com.enofex..", ClassCriteria.inPackage("com.enofex..").getDescription());
  }

  @Test
  void shouldMatchPackagesUsingArchUnitSyntax() {
    JavaClass imported = new ClassFileImporter().importClass(UserDTO.class);
    assertTrue(ClassCriteria.inPackage("com.enofex.taikai").test(imported));
    assertTrue(ClassCriteria.inPackage("com.enofex..").test(imported));
    assertTrue(ClassCriteria.inPackage("com.*.taikai").test(imported));
    assertFalse(ClassCriteria.inPackage("com.company.internal..").test(imported));
  }

  @Test
  void shouldComposeCriteriaWithAndAndOr() {
    JavaClass imported = new ClassFileImporter().importClass(UserEntity.class);
    assertTrue(ClassCriteria.annotatedWith(Entity.class)
        .and(ClassCriteria.inPackage("com.enofex.taikai")).test(imported));
    assertTrue(ClassCriteria.nameMatching(".*DTO").or(ClassCriteria.annotatedWith(Entity.class))
        .test(imported));
    assertFalse(ClassCriteria.nameMatching(".*DTO").and(ClassCriteria.annotatedWith(Entity.class))
        .test(imported));
  }

  @Test
  void shouldMatchSubtypesButNotTheTypeItself() {
    JavaClasses classes = new ClassFileImporter()
        .importClasses(Repository.class, UserRepository.class, CachedUserRepository.class,
            UserDTO.class);
    for (DescribedPredicate<JavaClass> criteria : List.of(
        ClassCriteria.assignableTo(Repository.class),
        ClassCriteria.assignableTo(Repository.class.getName()))) {
      assertTrue(criteria.test(classes.get(UserRepository.class)));
      assertTrue(criteria.test(classes.get(CachedUserRepository.class)));
      assertFalse(criteria.test(classes.get(Repository.class)));
      assertFalse(criteria.test(classes.get(UserDTO.class)));
    }
  }

  @Test
  void shouldMatchImplementationsButNoInterfaces() {
    JavaClasses classes = new ClassFileImporter()
        .importClasses(Repository.class, PagingRepository.class, UserRepository.class,
            CachedUserRepository.class, UserDTO.class);
    for (DescribedPredicate<JavaClass> criteria : List.of(
        ClassCriteria.implementing(Repository.class),
        ClassCriteria.implementing(Repository.class.getName()))) {
      assertTrue(criteria.test(classes.get(UserRepository.class)));
      assertTrue(criteria.test(classes.get(CachedUserRepository.class)));
      assertFalse(criteria.test(classes.get(PagingRepository.class)));
      assertFalse(criteria.test(classes.get(Repository.class)));
      assertFalse(criteria.test(classes.get(UserDTO.class)));
    }
    assertTrue(ClassCriteria.assignableTo(Repository.class)
        .test(classes.get(PagingRepository.class)));
  }

  @Test
  void shouldMatchExactlyTheType() {
    JavaClasses classes = new ClassFileImporter()
        .importClasses(UserRepository.class, CachedUserRepository.class);
    for (DescribedPredicate<JavaClass> criteria : List.of(
        ClassCriteria.type(UserRepository.class),
        ClassCriteria.type(UserRepository.class.getName()))) {
      assertTrue(criteria.test(classes.get(UserRepository.class)));
      assertFalse(criteria.test(classes.get(CachedUserRepository.class)));
    }
  }

  @Test
  void shouldDescribeTypeCriteria() {
    assertEquals("assignable to " + Repository.class.getName(),
        ClassCriteria.assignableTo(Repository.class).getDescription());
    assertEquals("implementing " + Repository.class.getName(),
        ClassCriteria.implementing(Repository.class).getDescription());
    assertEquals(Repository.class.getName(), ClassCriteria.type(Repository.class).getDescription());
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

  @Retention(RetentionPolicy.CLASS)
  @interface ClassRetained {
  }

  @Retention(RetentionPolicy.SOURCE)
  @interface SourceRetained {
  }

  @ClassRetained
  @SourceRetained
  static class RetentionFixture {
  }

  interface Repository {
  }

  interface PagingRepository extends Repository {
  }

  static class UserRepository implements Repository {
  }

  static class CachedUserRepository extends UserRepository {
  }
}
