package com.enofex.taikai.quarkus;

import com.enofex.taikai.Taikai;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PanacheConfigurerTest {

  @Nested
  class NamesShouldEndWithRepository {
    @Test
    void shouldNotThrowWhenRepositoryPatternClassEndsWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(PersonRepository.class)
          .quarkus(quarkus -> quarkus.panache(PanacheConfigurer::namesShouldEndWithRepository))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryPatternClassNotEndsWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(PersonDAO.class)
          .quarkus(quarkus -> quarkus.panache(PanacheConfigurer::namesShouldEndWithRepository))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ActiveRecordPatternShouldBeAnnotatedWithEntity {
    @Test
    void shouldNotThrowWhenActiveRecordPatternIsAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(Person.class)
          .quarkus(quarkus -> quarkus.panache(PanacheConfigurer::shouldBeAnnotatedWithEntityWhenActiveRecordPattern))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenActiveRecordPatternIsNotAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(PersonNoEntity.class)
          .quarkus(quarkus -> quarkus.panache(PanacheConfigurer::shouldBeAnnotatedWithEntityWhenActiveRecordPattern))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }


  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenRepositoryNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(PersonRepository.class)
          .quarkus(quarkus -> quarkus.panache(
              panache -> panache.namesShouldMatch(".+Repository")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(PersonDAO.class)
          .quarkus(quarkus -> quarkus.panache(
              panache -> panache.namesShouldMatch(".+Repository")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class AnnotatedWithEntityWhenActiveRecordPattern {

    @Test
    void shouldNotThrowWhenActiveRecordPatternIsAnnotatedViaAlias() {
      Taikai taikai = Taikai.builder()
          .classes(Person.class)
          .quarkus(quarkus -> quarkus.panache(PanacheConfigurer::annotatedWithEntityWhenActiveRecordPattern))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenActiveRecordPatternIsNotAnnotatedViaAlias() {
      Taikai taikai = Taikai.builder()
          .classes(PersonNoEntity.class)
          .quarkus(quarkus -> quarkus.panache(PanacheConfigurer::annotatedWithEntityWhenActiveRecordPattern))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(PersonRepository.class)
          .quarkus(quarkus -> quarkus.panache(
              panache -> panache.namesShouldEndWithRepository(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithEntityWhenActiveRecordPattern() {
      Taikai taikai = Taikai.builder()
          .classes(Person.class)
          .quarkus(quarkus -> quarkus.panache(
              panache -> panache.shouldBeAnnotatedWithEntityWhenActiveRecordPattern(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForAnnotatedWithEntityWhenActiveRecordPattern() {
      Taikai taikai = Taikai.builder()
          .classes(Person.class)
          .quarkus(quarkus -> quarkus.panache(
              panache -> panache.annotatedWithEntityWhenActiveRecordPattern(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
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
