package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

class RepositoriesConfigurerTest {

  @Nested
  class NamesShouldEndWithRepository {

    @Test
    void shouldNotThrowWhenRepositoryEndsWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(
              spring -> spring.repositories(RepositoriesConfigurer::namesShouldEndWithRepository))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedRepositoryDoesNotEndWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidRepoName.class)
          .spring(
              spring -> spring.repositories(RepositoriesConfigurer::namesShouldEndWithRepository))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNotAnnotatedAsRepository() {
      Taikai taikai = Taikai.builder()
          .classes(UtilityHelper.class)
          .spring(
              spring -> spring.repositories(RepositoriesConfigurer::namesShouldEndWithRepository))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithRepository {

    @Test
    void shouldNotThrowWhenRepositoryAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(
              RepositoriesConfigurer::shouldBeAnnotatedWithRepository))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryMissingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationRepository.class)
          .spring(spring -> spring.repositories(
              RepositoriesConfigurer::shouldBeAnnotatedWithRepository))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassDoesNotMatchRepositoryPattern() {
      Taikai taikai = Taikai.builder()
          .classes(UtilityHelper.class)
          .spring(spring -> spring.repositories(
              RepositoriesConfigurer::shouldBeAnnotatedWithRepository))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldNotDependOnServices {

    @Test
    void shouldNotThrowWhenRepositoryDoesNotDependOnService() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(RepositoriesConfigurer::shouldNotDependOnServices))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryDependsOnService() {
      Taikai taikai = Taikai.builder()
          .classes(RepositoryDependingOnService.class,
              UserService.class)
          .spring(spring -> spring.repositories(RepositoriesConfigurer::shouldNotDependOnServices))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Repository
  static class UserRepository {
    // valid repository
  }

  @Repository
  static class InvalidRepoName {

  }

  static class MissingAnnotationRepository {

  }

  static class UtilityHelper {

  }

  @Service
  static class UserService {

  }

  @Repository
  static class RepositoryDependingOnService {

    private final UserService userService;

    RepositoryDependingOnService(UserService userService) {
      this.userService = userService;
    }
  }
}
