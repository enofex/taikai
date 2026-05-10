package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ClassesShouldBeInterfacesTest {

  @Nested
  class ClassesShouldBeInterfaces {

    @Test
    void shouldNotThrowWhenMatchedTypeIsAnInterface() {
      Taikai taikai = Taikai.builder()
          .classes(MyRepository.class)
          .java(java -> java.classesShouldBeInterfaces(".*Repository"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchedTypeIsNotAnInterface() {
      Taikai taikai = Taikai.builder()
          .classes(ConcreteRepository.class)
          .java(java -> java.classesShouldBeInterfaces(".*Repository"))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNoClassMatchesPattern() {
      Taikai taikai = Taikai.builder()
          .classes(MyRepository.class)
          .java(java -> java.classesShouldBeInterfaces(".*NonExistent"))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportCustomConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(MyRepository.class)
          .java(java -> java.classesShouldBeInterfaces(
              ".*Repository",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  interface MyRepository {
    void findAll();
  }

  static class ConcreteRepository {
    public void findAll() {
    }
  }
}
