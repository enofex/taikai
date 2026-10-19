package com.enofex.taikai.jpa;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JpaConfigurerTest {

  @Nested
  class Entities {

    @Test
    void shouldRegisterEntityRules() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldBePackagePrivate))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldCombineRulesFromRepeatedCustomizations() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .jpa(jpa -> jpa.entities(entities -> entities.namesShouldMatch(".+Entity")))
          .jpa(jpa -> jpa.entities(EntitiesConfigurer::shouldBePackagePrivate))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableJpaConfigurerAndItsSubConfigurers() {
      Taikai taikai = Taikai.builder()
          .classes(PublicEntity.class)
          .jpa(jpa -> {
            jpa.entities(EntitiesConfigurer::shouldBePackagePrivate);
            jpa.disable();
          })
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnSameConfigurerFromDisable() {
      Taikai.builder()
          .jpa(jpa -> assertSame(jpa, jpa.disable()))
          .build();
    }
  }

  @Entity
  public static class PublicEntity {
    @Id
    private Long id;
  }
}
