package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

class ServicesConfigurerTest {

  @Nested
  class NamesShouldEndWithService {

    @Test
    void shouldNotThrowWhenServiceEndsWithService() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(ServicesConfigurer::namesShouldEndWithService))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedServiceDoesNotEndWithService() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNaming.class)
          .spring(spring -> spring.services(ServicesConfigurer::namesShouldEndWithService))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassIsNotAnnotatedWithService() {
      Taikai taikai = Taikai.builder()
          .classes(UtilityHelper.class)
          .spring(spring -> spring.services(ServicesConfigurer::namesShouldEndWithService))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithService {

    @Test
    void shouldNotThrowWhenAnnotatedWithService() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldBeAnnotatedWithService))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenServiceMissingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldBeAnnotatedWithService))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassDoesNotMatchServicePattern() {
      Taikai taikai = Taikai.builder()
          .classes(UtilityHelper.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldBeAnnotatedWithService))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldNotDependOnControllers {

    @Test
    void shouldNotThrowWhenServiceDoesNotDependOnController() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldNotDependOnControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenServiceDependsOnController() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceDependingOnController.class,
              UserController.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldNotDependOnControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenServiceDependsOnRestController() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceDependingOnRestController.class,
              ApiController.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldNotDependOnControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Service
  static class UserService {
    // valid service
  }

  @Service
  static class InvalidNaming {

  }

  static class MissingAnnotationService {

  }

  static class UtilityHelper {

  }

  @Controller
  static class UserController {

  }

  @RestController
  static class ApiController {

  }

  @Service
  static class ServiceDependingOnController {

    private final UserController controller;

    ServiceDependingOnController(UserController controller) {
      this.controller = controller;
    }
  }

  @Service
  static class ServiceDependingOnRestController {

    private final ApiController apiController;

    ServiceDependingOnRestController(ApiController apiController) {
      this.apiController = apiController;
    }
  }
}
