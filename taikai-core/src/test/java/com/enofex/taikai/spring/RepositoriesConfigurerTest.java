package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

class RepositoriesConfigurerTest {

  @Nested
  class ShouldBePackagePrivate {

    @Test
    void shouldNotThrowWhenRepositoryIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateRepository.class)
          .spring(spring -> spring.repositories(RepositoriesConfigurer::shouldBePackagePrivate))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenRepositoryIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicRepository.class)
          .spring(spring -> spring.repositories(RepositoriesConfigurer::shouldBePackagePrivate))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Repository
  static class PackagePrivateRepository {
  }

  @Repository
  public static class PublicRepository {
  }

  @Nested
  class NamesShouldEndWithRepository {

    static Stream<Named<Customizer<RepositoriesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithRepository()",
              repo -> repo.namesShouldEndWithRepository()),
          Named.of("namesShouldEndWithRepository(Configuration)",
              repo -> repo.namesShouldEndWithRepository(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              repo -> repo.namesShouldMatch(".+Repository")),
          Named.of("namesShouldMatch(String, Configuration)",
              repo -> repo.namesShouldMatch(".+Repository", defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenRepositoryEndsWithRepository(Customizer<RepositoriesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenAnnotatedRepositoryDoesNotEndWithRepository(
        Customizer<RepositoriesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(InvalidRepoName.class)
          .spring(spring -> spring.repositories(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithRepository {

    static Stream<Named<Customizer<RepositoriesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithRepository()",
              repo -> repo.shouldBeAnnotatedWithRepository()),
          Named.of("shouldBeAnnotatedWithRepository(Configuration)",
              repo -> repo.shouldBeAnnotatedWithRepository(defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithRepository(DescribedPredicate)",
              repo -> repo.shouldBeAnnotatedWithRepository(
                  ClassCriteria.nameMatching(".+Repository"))),
          Named.of("shouldBeAnnotatedWithRepository(DescribedPredicate, Configuration)",
              repo -> repo.shouldBeAnnotatedWithRepository(
                  ClassCriteria.nameMatching(".+Repository"), defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenRepositoryAnnotated(Customizer<RepositoriesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserRepository.class)
          .spring(spring -> spring.repositories(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenRepositoryMissingAnnotation(Customizer<RepositoriesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationRepository.class)
          .spring(spring -> spring.repositories(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenRepositoryInterfaceIsNotAnnotated(
        Customizer<RepositoriesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(SpringDataRepository.class)
          .spring(spring -> spring.repositories(rule))
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
  class UnrelatedClasses {

    static Stream<Named<Customizer<RepositoriesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithRepository()",
              repo -> repo.namesShouldEndWithRepository()),
          Named.of("shouldBeAnnotatedWithRepository()",
              repo -> repo.shouldBeAnnotatedWithRepository()),
          Named.of("shouldNotDependOnControllers()",
              repo -> repo.shouldNotDependOnControllers()),
          Named.of("shouldNotDependOnServices()",
              repo -> repo.shouldNotDependOnServices()),
          Named.of("shouldBePackagePrivate()",
              repo -> repo.shouldBePackagePrivate()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenClassIsNotARepository(Customizer<RepositoriesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UtilityHelper.class)
          .spring(spring -> spring.repositories(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Repository
  static class UserRepository {
    // valid repository
  }

  @Repository
  static class InvalidRepoName {

  }

  interface SpringDataRepository {
  }

  static class MissingAnnotationRepository {

  }

  public static class UtilityHelper {

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
