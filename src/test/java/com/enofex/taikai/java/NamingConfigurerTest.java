package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NamingConfigurerTest {

  @Nested
  class ClassesShouldMatch {

    @Test
    void shouldNotThrowWhenClassNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(".*Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNamingClass.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(".*Service")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ValidService.class)
          .java(java -> java.naming(naming -> naming.classesShouldMatch(
              ".*Service",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class MethodsShouldMatch {

    @Test
    void shouldNotThrowWhenMethodNameMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCamelCaseMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch("[a-z][a-zA-Z0-9]*")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMethodNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithBadMethodName.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch("[a-z][a-zA-Z0-9]*")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ClassWithCamelCaseMethod.class)
          .java(java -> java.naming(naming -> naming.methodsShouldMatch(
              "[a-z][a-zA-Z0-9]*",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class ValidService {
  }

  static class InvalidNamingClass {
  }

  static class ClassWithCamelCaseMethod {
    void doSomething() {
    }
  }

  static class ClassWithBadMethodName {
    void Do_Something() {
    }
  }
}
