package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

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
  class ShouldNotDependOnControllers {

    @Test
    void shouldNotThrowWhenRepositoryDoesNotDependOnController() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(
              RepositoriesConfigurer::shouldNotDependOnControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryDependsOnController() {
      Taikai taikai = Taikai.builder()
          .classes(RepositoryDependingOnController.class, UserController.class)
          .spring(spring -> spring.repositories(
              RepositoriesConfigurer::shouldNotDependOnControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryDependsOnRestController() {
      Taikai taikai = Taikai.builder()
          .classes(RepositoryDependingOnRestController.class, ApiController.class)
          .spring(spring -> spring.repositories(
              RepositoriesConfigurer::shouldNotDependOnControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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

  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenRepositoryNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(
              repo -> repo.namesShouldMatch(".+Repository")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidRepoName.class)
          .spring(spring -> spring.repositories(
              repo -> repo.namesShouldMatch(".+Repository")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithRepositoryByRegex {

    @Test
    void shouldNotThrowWhenMatchingClassIsAnnotatedWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(
              repo -> repo.shouldBeAnnotatedWithRepository(".+Repository")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassMissingRepositoryAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationRepository.class)
          .spring(spring -> spring.repositories(
              repo -> repo.shouldBeAnnotatedWithRepository(".+Repository")))
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

  @Controller
  static class UserController {

  }

  @RestController
  static class ApiController {

  }

  @Repository
  static class RepositoryDependingOnController {

    private final UserController userController;

    RepositoryDependingOnController(UserController userController) {
      this.userController = userController;
    }
  }

  @Repository
  static class RepositoryDependingOnRestController {

    private final ApiController apiController;

    RepositoryDependingOnRestController(ApiController apiController) {
      this.apiController = apiController;
    }
  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(
              repo -> repo.namesShouldEndWithRepository(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithRepository() {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(
              repo -> repo.shouldBeAnnotatedWithRepository(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableRepositoriesConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidRepoName.class)
          .spring(spring -> spring.repositories(repo -> {
            repo.namesShouldEndWithRepository();
            repo.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }
}
