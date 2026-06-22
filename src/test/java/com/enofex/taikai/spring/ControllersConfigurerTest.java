package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class ControllersConfigurerTest {

  @Nested
  class NamesShouldEndWithController {

    @Test
    void shouldNotThrowWhenControllerEndsWithController() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(ControllersConfigurer::namesShouldEndWithController))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassIsNotControllerOrRestController() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidUserHandler.class)
          .spring(spring -> spring.controllers(ControllersConfigurer::namesShouldEndWithController))
          .build();

      assertDoesNotThrow(taikai::check);
    }

  }

  @Nested
  class ShouldBeAnnotatedWithRestController {

    @Test
    void shouldNotThrowWhenRestControllerAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithRestController))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenControllerMissingRestControllerAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithRestController))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithController {

    @Test
    void shouldNotThrowWhenControllerAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(BasicController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithController))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMissingControllerAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(UnannotatedController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithController))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBePackagePrivate {

    @Test
    void shouldThrowWhenControllerIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(PublicController.class)
          .spring(spring -> spring.controllers(ControllersConfigurer::shouldBePackagePrivate))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenControllerIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(PackagePrivateController.class)
          .spring(spring -> spring.controllers(ControllersConfigurer::shouldBePackagePrivate))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithValidated {

    @Test
    void shouldNotThrowWhenControllerIsValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedUserController.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
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

    @Test
    void shouldThrowWhenControllerHasValidationAnnotationsButMissingValidatedAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(
              UnvalidatedControllerWithValidationAnnotations.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenControllerHasMinAndSizeValidationAnnotationsButMissingValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ControllerWithMinAndSizeAnnotations.class)
          .spring(spring -> spring.controllers(
              ControllersConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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

  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenControllerNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.namesShouldMatch(".+Controller")))
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
  class ShouldBeAnnotatedWithRestControllerByRegex {

    @Test
    void shouldNotThrowWhenMatchingClassHasRestControllerAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(".+Controller")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassMissingRestControllerAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingRestController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(".+Controller")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithControllerByRegex {

    @Test
    void shouldNotThrowWhenMatchingClassHasControllerAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(BasicController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithController(".+Controller")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassMissingControllerAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(UnannotatedController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithController(".+Controller")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
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
  class ShouldBeAnnotatedWithValidatedByRegex {

    @Test
    void shouldThrowWhenMatchingControllerHasValidationAnnotationsButMissingValidated() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedApiController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithValidated(".+Controller")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMatchingControllerIsValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedUserController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithValidated(".+Controller")))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @RestController
  static class UnvalidatedApiController {

    public String getUser(@RequestParam @NotNull String id) {
      return id;
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

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithController() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.namesShouldEndWithController(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithRestController() {
      Taikai taikai = Taikai.builder()
          .classes(ValidUserController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithController() {
      Taikai taikai = Taikai.builder()
          .classes(BasicController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithController(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
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
    void disable_returnsThisForChaining() {
      Configurers configurers = new Configurers();
      ConfigurerContext context = new ConfigurerContext("test", configurers);
      ControllersConfigurer configurer = new ControllersConfigurer(context);
      ControllersConfigurer result = configurer.disable();
      assertNotNull(result);
      assertSame(configurer, result);
    }

    @Test
    void allFluentMethods_returnThisForChaining() {
      Configurers configurers = new Configurers();
      ConfigurerContext context = new ConfigurerContext("test", configurers);
      ControllersConfigurer configurer = new ControllersConfigurer(context);

      assertSame(configurer, configurer.namesShouldEndWithController());
      assertSame(configurer, configurer.namesShouldEndWithController(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.namesShouldMatch(".*"));
      assertSame(configurer, configurer.namesShouldMatch(".*", Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController());
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController(".*"));
      assertSame(configurer, configurer.shouldBeAnnotatedWithRestController(".*", Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithController());
      assertSame(configurer, configurer.shouldBeAnnotatedWithController(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithController(".*"));
      assertSame(configurer, configurer.shouldBeAnnotatedWithController(".*", Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBePackagePrivate());
      assertSame(configurer, configurer.shouldBePackagePrivate(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldNotDependOnOtherControllers());
      assertSame(configurer, configurer.shouldNotDependOnOtherControllers(Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated(".*"));
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated(".*", Configuration.defaultConfiguration()));
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated());
      assertSame(configurer, configurer.shouldBeAnnotatedWithValidated(Configuration.defaultConfiguration()));
    }
  }

  // === Negative tests for delegation methods (kills NakedReceiverMutator) ===

  @Nested
  class NegativeTests {

    @Test
    void namesShouldEndWithController_whenNameViolates_thenThrows() {
      Taikai taikai = Taikai.builder()
          .classes(ViolatingHandler.class)
          .spring(spring -> spring.controllers(ControllersConfigurer::namesShouldEndWithController))
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void namesShouldEndWithController_withConfig_whenNameViolates_thenThrows() {
      Taikai taikai = Taikai.builder()
          .classes(ViolatingHandler.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.namesShouldEndWithController(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldBeAnnotatedWithRestController_withConfig_whenNotAnnotated_thenThrows() {
      Taikai taikai = Taikai.builder()
          .classes(UnannotatedController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithRestController(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldBeAnnotatedWithController_withConfig_whenNotAnnotated_thenThrows() {
      Taikai taikai = Taikai.builder()
          .classes(UnannotatedController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBeAnnotatedWithController(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldBePackagePrivate_withConfig_whenPublic_thenThrows() {
      Taikai taikai = Taikai.builder()
          .classes(PublicController.class)
          .spring(spring -> spring.controllers(
              ctrl -> ctrl.shouldBePackagePrivate(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();
      assertThrows(AssertionError.class, taikai::check);
    }
  }

  // === Test helper classes for negative tests ===

  @RestController
  static class ViolatingHandler {
    public String get() { return "test"; }
  }
}
