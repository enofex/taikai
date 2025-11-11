package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import jakarta.validation.constraints.NotNull;
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
}
