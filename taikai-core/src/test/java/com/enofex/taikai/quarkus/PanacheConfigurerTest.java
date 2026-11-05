package com.enofex.taikai.quarkus;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.persistence.Entity;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PanacheConfigurerTest {

  @Nested
  class NamesShouldEndWithRepository {

    static Stream<Named<Customizer<PanacheConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithRepository()",
              panache -> panache.namesShouldEndWithRepository()),
          Named.of("namesShouldEndWithRepository(Configuration)",
              panache -> panache.namesShouldEndWithRepository(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              panache -> panache.namesShouldMatch(".+Repository")),
          Named.of("namesShouldMatch(String, Configuration)",
              panache -> panache.namesShouldMatch(".+Repository", defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenRepositoryPatternClassEndsWithRepository(
        Customizer<PanacheConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PersonRepository.class)
          .quarkus(quarkus -> quarkus.panache(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenRepositoryPatternClassDoesNotEndWithRepository(
        Customizer<PanacheConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PersonDAO.class)
          .quarkus(quarkus -> quarkus.panache(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ActiveRecordPatternShouldBeAnnotatedWithEntity {

    static Stream<Named<Customizer<PanacheConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithEntityWhenActiveRecordPattern()",
              panache -> panache.shouldBeAnnotatedWithEntityWhenActiveRecordPattern()),
          Named.of("shouldBeAnnotatedWithEntityWhenActiveRecordPattern(Configuration)",
              panache -> panache.shouldBeAnnotatedWithEntityWhenActiveRecordPattern(
                  defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenActiveRecordPatternIsAnnotated(Customizer<PanacheConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(Person.class)
          .quarkus(quarkus -> quarkus.panache(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenActiveRecordPatternIsNotAnnotated(Customizer<PanacheConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PersonNoEntity.class)
          .quarkus(quarkus -> quarkus.panache(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisablePanacheConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(PersonDAO.class)
          .quarkus(quarkus -> quarkus.panache(panache -> {
            panache.namesShouldEndWithRepository();
            panache.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Entity
  static class Person extends PanacheEntity {
  }

  static class PersonNoEntity extends PanacheEntity {
  }

  static class PersonRepository implements PanacheRepository<String> {
  }

  static class PersonDAO implements PanacheRepository<String> {
  }
}
