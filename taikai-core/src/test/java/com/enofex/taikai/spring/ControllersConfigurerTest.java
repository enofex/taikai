package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import com.enofex.taikai.configures.Customizer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class ControllersConfigurerTest {

  @Nested
  class NamesShouldEndWithController {

    static Stream<Named<Customizer<ControllersConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithController()",
              ctrl -> ctrl.namesShouldEndWithController()),
          Named.of("namesShouldEndWithController(Configuration)",
              ctrl -> ctrl.namesShouldEndWithController(Configuration.defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              ctrl -> ctrl.namesShouldMatch(".+Controller")),
          Named.of("namesShouldMatch(String, Configuration)",
              ctrl -> ctrl.namesShouldMatch(".+Controller", Configuration.defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerEndsWithController(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenAnnotatedControllerDoesNotEndWithController(
        Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ViolatingHandler.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassIsNotControllerOrRestController() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidUserHandler.class)
          .spring(spring -> spring.controllers(ControllersConfigurer::namesShouldEndWithController))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenControllerNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.namesShouldMatch(".+Handler")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithRestController {

    static Stream<Named<Customizer<ControllersConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithRestController()",
              ctrl -> ctrl.shouldBeAnnotatedWithRestController()),
          Named.of("shouldBeAnnotatedWithRestController(Configuration)",
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(
                  Configuration.defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithRestController(DescribedPredicate)",
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(
                  ClassCriteria.nameMatching(".+Controller"))),
          Named.of("shouldBeAnnotatedWithRestController(DescribedPredicate, Configuration)",
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(
                  ClassCriteria.nameMatching(".+Controller"),
                  Configuration.defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenRestControllerAnnotated(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenControllerMissingRestControllerAnnotation(
        Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerInterfaceIsNotAnnotated(
        Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(OrderController.class, DefaultOrderController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithController {

    static Stream<Named<Customizer<ControllersConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithController()",
              ctrl -> ctrl.shouldBeAnnotatedWithController()),
          Named.of("shouldBeAnnotatedWithController(Configuration)",
              ctrl -> ctrl.shouldBeAnnotatedWithController(Configuration.defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithController(DescribedPredicate)",
              ctrl -> ctrl.shouldBeAnnotatedWithController(
                  ClassCriteria.nameMatching(".+Controller"))),
          Named.of("shouldBeAnnotatedWithController(DescribedPredicate, Configuration)",
              ctrl -> ctrl.shouldBeAnnotatedWithController(
                  ClassCriteria.nameMatching(".+Controller"),
                  Configuration.defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerAnnotated(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(BasicController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenMissingControllerAnnotation(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UnannotatedController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerInterfaceIsNotAnnotated(
        Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(OrderController.class, DefaultOrderController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBePackagePrivate {

    static Stream<Named<Customizer<ControllersConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBePackagePrivate()",
              ctrl -> ctrl.shouldBePackagePrivate()),
          Named.of("shouldBePackagePrivate(Configuration)",
              ctrl -> ctrl.shouldBePackagePrivate(Configuration.defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenControllerIsPublic(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PublicController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerIsPackagePrivate(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithValidated {

    static Stream<Named<Customizer<ControllersConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithValidated()",
              ctrl -> ctrl.shouldBeAnnotatedWithValidated()),
          Named.of("shouldBeAnnotatedWithValidated(Configuration)",
              ctrl -> ctrl.shouldBeAnnotatedWithValidated(Configuration.defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithValidated(DescribedPredicate)",
              ctrl -> ctrl.shouldBeAnnotatedWithValidated(
                  ClassCriteria.nameMatching(".+Controller"))),
          Named.of("shouldBeAnnotatedWithValidated(DescribedPredicate, Configuration)",
              ctrl -> ctrl.shouldBeAnnotatedWithValidated(
                  ClassCriteria.nameMatching(".+Controller"),
                  Configuration.defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerIsValidated(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedUserController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenControllerIsNotValidated(Customizer<ControllersConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedApiController.class)
          .spring(spring -> spring.controllers(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenControllerHasNoValidationAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {UnvalidatedControllerWithValidationAnnotations.class,
        ControllerWithMinAndSizeAnnotations.class})
    void shouldThrowWhenControllerHasValidationAnnotationsButMissingValidatedAnnotation(
        Class<?> controller) {
      Taikai taikai = Taikai.builder()
          .classes(controller)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @RestController
  static class UnvalidatedApiController {

    public String getUser(@RequestParam @NotNull String id) {
      return id;
    }
  }

  @Nested
  class ShouldNotBeAnnotatedWithValidated {

    @Test
    void shouldThrowWhenControllerIsValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotBeAnnotatedWithValidated))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenControllerHasNoValidationAnnotations() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenControllerHasValidationAnnotationsButMissingValidatedAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedControllerWithValidationAnnotations.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenControllerHasMinAndSizeValidationAnnotationsButMissingValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ControllerWithMinAndSizeAnnotations.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @RestController
  static class ValidUserController {

  }

  @Controller
  static class BasicController {

  }

  static class InvalidUserHandler {

  }

  @Controller
  static class MissingRestController {

  }

  @Controller
  public static class PublicController {

  }

  @Controller
  static class PackagePrivateController {

  }

  @RestController
  @Validated
  static class ValidatedUserController {

  }

  @RestController
  static class UnvalidatedUserController {

  }

  @RestController
  static class UnvalidatedControllerWithValidationAnnotations {

    public String getUser(@RequestParam @NotNull String id) {
      return id;
    }

    public String getByPath(@PathVariable @NotNull String userId) {
      return userId;
    }
  }

  static class UnannotatedController {

  }

  interface OrderController {

  }

  @RestController
  static class DefaultOrderController implements OrderController {

  }

  @Nested
  class ShouldNotDependOnOtherControllers {

    @Test
    void shouldNotThrowWhenControllerDoesNotDependOnOtherController() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotDependOnOtherControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenControllerDependsOnOtherController() {
      Taikai taikai = Taikai.builder()
          .classes(ControllerDependingOnAnotherController.class, ValidUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotDependOnOtherControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldNotDependOnRepositories {

    @Test
    void shouldNotThrowWhenControllerDoesNotDependOnRepository() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotDependOnRepositories))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenControllerDependsOnService() {
      Taikai taikai = Taikai.builder()
          .classes(ControllerDependingOnService.class, OrderService.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotDependOnRepositories))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassIsNotControllerOrRestController() {
      Taikai taikai = Taikai.builder()
          .classes(HandlerDependingOnRepository.class, OrderRepository.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotDependOnRepositories))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {RestControllerDependingOnRepository.class,
        ControllerDependingOnRepository.class})
    void shouldThrowWhenControllerDependsOnRepository(Class<?> controller) {
      Taikai taikai = Taikai.builder()
          .classes(controller, OrderRepository.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldNotDependOnRepositories))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenControllerDependsOnRepositoryWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(RestControllerDependingOnRepository.class, OrderRepository.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldNotDependOnRepositories(Configuration.defaultConfiguration())))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @RestController
  static class ControllerWithMinAndSizeAnnotations {

    public String getById(@PathVariable @Min(1) long id) {
      return String.valueOf(id);
    }

    public String getByName(@PathVariable @Size(min = 1) String name) {
      return name;
    }

    public String getByMaxId(@PathVariable @Max(100) long maxId) {
      return String.valueOf(maxId);
    }

    public String getByTitle(@RequestParam @NotBlank String title) {
      return title;
    }

    public String getByPattern(@RequestParam @Pattern(regexp = ".*") String pattern) {
      return pattern;
    }
  }

  @RestController
  static class ControllerDependingOnAnotherController {

    private final ValidUserController other;

    ControllerDependingOnAnotherController(ValidUserController other) {
      this.other = other;
    }
  }

  @Repository
  static class OrderRepository {

  }

  @Service
  static class OrderService {

  }

  @RestController
  static class RestControllerDependingOnRepository {

    private final OrderRepository orderRepository;

    RestControllerDependingOnRepository(OrderRepository orderRepository) {
      this.orderRepository = orderRepository;
    }
  }

  @Controller
  static class ControllerDependingOnRepository {

    private final OrderRepository orderRepository;

    ControllerDependingOnRepository(OrderRepository orderRepository) {
      this.orderRepository = orderRepository;
    }
  }

  @RestController
  static class ControllerDependingOnService {

    private final OrderService orderService;

    ControllerDependingOnService(OrderService orderService) {
      this.orderService = orderService;
    }
  }

  static class HandlerDependingOnRepository {

    private final OrderRepository orderRepository;

    HandlerDependingOnRepository(OrderRepository orderRepository) {
      this.orderRepository = orderRepository;
    }
  }

  @RestController
  static class ViolatingHandler {
    public String get() { return "test"; }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableControllersConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestController.class)
          .spring(spring -> spring.controllers(ctrl -> {
            ctrl.shouldBeAnnotatedWithRestController();
            ctrl.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldReturnSameConfigurerFromAllFluentMethods() {
      Configurers configurers = new Configurers();
      ConfigurerContext context = new ConfigurerContext("test", configurers);
      ControllersConfigurer configurer = new ControllersConfigurer(context);

      assertSame(configurer, configurer.namesShouldEndWithController());
      assertSame(configurer, configurer.namesShouldEndWithController(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.namesShouldMatch(".*"));
      assertSame(configurer, configurer.namesShouldMatch(".*", Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController());
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching(".*")));
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching(".*"), Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithController());
      assertSame(configurer, configurer.shouldBeAnnotatedWithController(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithController(ClassCriteria.nameMatching(".*")));
      assertSame(configurer, configurer.shouldBeAnnotatedWithController(ClassCriteria.nameMatching(".*"), Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBePackagePrivate());
      assertSame(configurer, configurer.shouldBePackagePrivate(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldNotDependOnOtherControllers());
      assertSame(configurer, configurer.shouldNotDependOnOtherControllers(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldNotDependOnRepositories());
      assertSame(configurer, configurer.shouldNotDependOnRepositories(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*")));
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*"), Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated());
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldNotBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*")));
      assertSame(configurer, configurer.shouldNotBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*"), Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldNotBeAnnotatedWithValidated());
      assertSame(configurer, configurer.shouldNotBeAnnotatedWithValidated(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.disable());
    }
  }
}
