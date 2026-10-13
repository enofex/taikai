package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Namespace;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;

class MethodReturnTypeTest {

  @ParameterizedTest
  @MethodSource("entitySignatures")
  void shouldRejectEntitiesAnywhereInReturnSignature(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.inPackage("com.enofex.taikai.java"), ClassCriteria.annotatedWith(Entity.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains("result"));
    assertTrue(failure.getMessage().contains(UserEntity.class.getName()));
  }

  static Stream<Class<?>> entitySignatures() {
    return Stream.of(DirectReturn.class, ArrayReturn.class, NestedReturn.class,
        UpperWildcardReturn.class, LowerWildcardReturn.class, TypeVariableReturn.class,
        GenericArrayReturn.class, RecursiveBoundReturn.class);
  }

  @ParameterizedTest
  @MethodSource("entitySignatures")
  void shouldAcceptRequiredTypesAnywhereInReturnSignature(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.inPackage("com.enofex.taikai.java"), ClassCriteria.annotatedWith(Entity.class)))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldRejectEachMethodWithoutRequiredReturnType() {
    Taikai taikai = Taikai.builder()
        .classes(SafeReturn.class)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.simpleNameMatching("SafeReturn"), ClassCriteria.annotatedWith(Entity.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains("result()"));
    assertTrue(failure.getMessage().contains("nothing()"));
    assertTrue(failure.getMessage().contains("count()"));
  }

  @Test
  void shouldRequireClassNamesEndingInDtoIncludingWrappedReturns() {
    Taikai taikai = Taikai.builder()
        .classes(DtoReturn.class)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.simpleNameMatching("DtoReturn"), ClassCriteria.nameMatching(".*DTO")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldAcceptRequiredTypesSelectedByPackage() {
    Taikai taikai = Taikai.builder()
        .classes(NestedReturn.class)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.simpleNameMatching("NestedReturn"),
            ClassCriteria.inPackage("org.springframework.http")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldIgnoreUnselectedClassesForRequiredReturns() {
    Taikai taikai = Taikai.builder()
        .classes(DirectReturn.class, SafeReturn.class)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.simpleNameMatching("DirectReturn"),
            ClassCriteria.annotatedWith(Entity.class.getName())))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldHonorConfigurationExclusionsForRequiredReturns() {
    Taikai taikai = Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.simpleNameMatching("DirectReturn|SafeReturn"),
            ClassCriteria.annotatedWith(Entity.class),
            Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(SafeReturn.class.getName()))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldAllowNoMatchingClassesForRequiredReturns() {
    Taikai taikai = Taikai.builder()
        .classes(SafeReturn.class)
        .java(java -> java.methodsShouldReturnForClass(
            ClassCriteria.inPackage("com.company.controllers.."), ClassCriteria.nameMatching(".*DTO")))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldCombineRequiredAndForbiddenReturnTypes() {
    Taikai taikai = Taikai.builder()
        .classes(DtoReturn.class)
        .java(java -> java
            .methodsShouldReturnForClass(ClassCriteria.simpleNameMatching("DtoReturn"),
                ClassCriteria.nameMatching(".*DTO"))
            .methodsShouldNotReturnForClass(ClassCriteria.simpleNameMatching("DtoReturn"),
                ClassCriteria.annotatedWith(Entity.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains(UserEntity.class.getName()));
  }

  @Test
  void shouldRejectTypesSelectedByNameOrPackage() {
    Taikai nameRule = Taikai.builder()
        .classes(DirectReturn.class)
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.simpleNameMatching("DirectReturn"), ClassCriteria.nameMatching(".*Entity")))
        .build();

    assertThrows(AssertionError.class, nameRule::check);

    Taikai packageRule = Taikai.builder()
        .classes(NestedReturn.class)
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.simpleNameMatching("NestedReturn"),
            ClassCriteria.inPackage("org.springframework.http")))
        .build();

    assertThrows(AssertionError.class, packageRule::check);
  }

  @Test
  void shouldAllowSafeReturnsAndUnselectedClasses() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(SafeReturn.class, DirectReturn.class)
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.simpleNameMatching("SafeReturn"),
            ClassCriteria.annotatedWith(Entity.class.getName())))
        .build().check());
  }

  @Test
  void shouldAllowForbiddenTypesOutsideTheReturnSignature() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(UsesEntity.class)
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.simpleNameMatching("UsesEntity"), ClassCriteria.annotatedWith(Entity.class)))
        .build().check());
  }

  @Test
  void shouldHonorConfigurationExclusions() {
    assertDoesNotThrow(() -> Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.simpleNameMatching("DirectReturn|SafeReturn"),
            ClassCriteria.annotatedWith(Entity.class),
            Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(DirectReturn.class.getName()))))
        .build().check());
  }

  @Test
  void shouldAllowNoMatchingClasses() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(DirectReturn.class)
        .java(java -> java.methodsShouldNotReturnForClass(
            ClassCriteria.inPackage("com.company.controllers.."), ClassCriteria.annotatedWith(Entity.class)))
        .build().check());
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Entity
  static class UserEntity {
  }

  static class UserDTO {
  }

  static class DtoReturn {
    UserDTO result() {
      return null;
    }

    ResponseEntity<List<UserDTO>> wrapped() {
      return null;
    }

    Map<UserEntity, UserDTO> mixed() {
      return null;
    }
  }

  static class DirectReturn {
    // ArchUnit inspects this method's bytecode to verify private return signatures.
    @SuppressWarnings("java:S1144")
    private UserEntity result() {
      return null;
    }
  }

  static class ArrayReturn {
    UserEntity[][] result() {
      return null;
    }
  }

  static class NestedReturn {
    ResponseEntity<Map<String, List<UserEntity>>> result() {
      return null;
    }
  }

  static class UpperWildcardReturn {
    List<? extends UserEntity> result() {
      return null;
    }
  }

  static class LowerWildcardReturn {
    List<? super UserEntity> result() {
      return null;
    }
  }

  static class TypeVariableReturn<T extends UserEntity> {
    T result() {
      return null;
    }
  }

  static class GenericArrayReturn {
    <T extends UserEntity> T[] result() {
      return null;
    }
  }

  static class RecursiveBoundReturn {
    <T extends UserEntity & Comparable<T>> T result() {
      return null;
    }
  }

  static class SafeReturn {
    List<String> result() {
      return null;
    }

    void nothing() {
      // This fixture verifies that void return types are allowed.
    }

    int count() {
      return 0;
    }
  }

  static class UsesEntity {
    private UserEntity entity;

    String result(UserEntity argument) {
      this.entity = argument;
      return "safe";
    }
  }
}
