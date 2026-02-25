package com.enofex.taikai.quarkus;

import com.enofex.taikai.Taikai;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuarkusConfigurerTest {

  @Nested
  class FieldsNotUsingInject {

    @Test
    void shouldThrowAnExceptionWhenFieldsAreAnnotatedWithInject() {

      Taikai taikai = Taikai.builder()
          .classes(Injection.class)
          .quarkus(quarkus ->
              quarkus.noInjectionFields())
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowAnExceptionWhenFieldsAreAnnotatedWithInjectAndAParameterConstructor() {

      Taikai taikai = Taikai.builder()
          .classes(InjectionWithConstructor.class)
          .quarkus(quarkus ->
              quarkus.noInjectionFields())
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shoutNotThrowAnExceptionIfOnlyConstructorUsed() {

      Taikai taikai = Taikai.builder()
          .classes(NoInjection.class)
          .quarkus(quarkus ->
              quarkus.noInjectionFields())
          .build();
      assertDoesNotThrow(taikai::check);
    }
  }

  static class Injection {

    @Inject
    String name;

  }

  static class InjectionWithConstructor {

    @Inject
    String name;

    public InjectionWithConstructor(String name) {
      this.name = name;
    }

  }

  static class NoInjection {

    String name;

    public NoInjection(String name) {
      this.name = name;
    }

  }

}
