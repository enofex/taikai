package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Criteria;
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
  @MethodSource("forbiddenSignatures")
  void shouldRejectEntitiesAnywhereInReturnSignature(Class<?> fixture) {
    Taikai taikai = Taikai.builder()
        .classes(fixture)
        .java(java -> java.methodsShouldNotReturn(
            Criteria.inPackage("com.enofex.taikai.java"), Criteria.annotatedWith(Entity.class)))
        .build();

    AssertionError failure = assertThrows(AssertionError.class, taikai::check);
    assertTrue(failure.getMessage().contains("result"));
    assertTrue(failure.getMessage().contains(UserEntity.class.getName()));
  }

  static Stream<Class<?>> forbiddenSignatures() {
    return Stream.of(DirectReturn.class, ArrayReturn.class, NestedReturn.class,
        UpperWildcardReturn.class, LowerWildcardReturn.class, TypeVariableReturn.class,
        GenericArrayReturn.class, RecursiveBoundReturn.class);
  }

  @Test
  void shouldRejectTypesSelectedByNameOrPackage() {
    Taikai nameRule = Taikai.builder()
        .classes(DirectReturn.class)
        .java(java -> java.methodsShouldNotReturn(
            Criteria.namedLike("DirectReturn"), Criteria.namedLike(".*Entity")))
        .build();

    assertThrows(AssertionError.class, nameRule::check);

    Taikai packageRule = Taikai.builder()
        .classes(NestedReturn.class)
        .java(java -> java.methodsShouldNotReturn(
            Criteria.namedLike("NestedReturn"), Criteria.inPackage("org.springframework.http")))
        .build();

    assertThrows(AssertionError.class, packageRule::check);
  }

  @Test
  void shouldAllowSafeReturnsAndUnselectedClasses() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(SafeReturn.class, DirectReturn.class)
        .java(java -> java.methodsShouldNotReturn(
            Criteria.namedLike("SafeReturn"), Criteria.annotatedWith(Entity.class.getName())))
        .build().check());
  }

  @Test
  void shouldAllowForbiddenTypesOutsideTheReturnSignature() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(UsesEntity.class)
        .java(java -> java.methodsShouldNotReturn(
            Criteria.namedLike("UsesEntity"), Criteria.annotatedWith(Entity.class)))
        .build().check());
  }

  @Test
  void shouldHonorConfigurationExclusions() {
    assertDoesNotThrow(() -> Taikai.builder()
        .namespace("com.enofex.taikai.java")
        .java(java -> java.methodsShouldNotReturn(
            Criteria.namedLike("DirectReturn|SafeReturn"), Criteria.annotatedWith(Entity.class),
            Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(DirectReturn.class.getName()))))
        .build().check());
  }

  @Test
  void shouldAllowNoMatchingClasses() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(DirectReturn.class)
        .java(java -> java.methodsShouldNotReturn(
            Criteria.inPackage("com.company.controllers.."), Criteria.annotatedWith(Entity.class)))
        .build().check());
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Entity {
  }

  @Entity
  static class UserEntity {
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
