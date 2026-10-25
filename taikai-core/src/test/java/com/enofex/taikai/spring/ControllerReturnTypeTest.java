package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Namespace;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

class ControllerReturnTypeTest {

  @ParameterizedTest
  @MethodSource("controllers")
  void shouldRejectForbiddenControllerReturns(Class<?> controller) {
    Taikai taikai = Taikai.builder()
        .classes(controller)
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldNotReturn(ClassCriteria.simpleNameMatching("InternalDTO"))))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  static Stream<Class<?>> controllers() {
    return Stream.of(MvcController.class, RestApi.class, ComposedController.class);
  }

  @ParameterizedTest
  @MethodSource("controllers")
  void shouldRequireControllerReturnsEndingInDto(Class<?> controller) {
    Taikai taikai = Taikai.builder()
        .classes(controller, SafeReturnOutsideController.class)
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldReturn(ClassCriteria.nameMatching(".*DTO"))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  @Test
  void shouldRejectControllersWithoutRequiredReturnTypes() {
    Taikai taikai = Taikai.builder()
        .classes(SafeController.class)
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldReturn(ClassCriteria.nameMatching(".*DTO"))))
        .build();

    assertThrows(AssertionError.class, taikai::check);
  }

  @Test
  void shouldHonorConfiguredClassesForRequiredReturns() {
    Taikai taikai = Taikai.builder()
        .classes(SafeController.class)
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldReturn(ClassCriteria.nameMatching(".*DTO"), Configuration.of(
                new ClassFileImporter().importClasses(
                    MvcController.class, RestApi.class, ComposedController.class)))))
        .build();

    assertDoesNotThrow(taikai::check);
  }

  static class SafeReturnOutsideController {
    String result() {
      return "safe";
    }
  }

  @Test
  void shouldAllowSafeControllersAndIgnoreOtherClasses() {
    assertDoesNotThrow(() -> Taikai.builder()
        .classes(SafeController.class, NonController.class)
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldNotReturn(ClassCriteria.simpleNameMatching("InternalDTO"))))
        .build().check());
  }

  @Test
  void shouldHonorConfigurationExclusions() {
    assertDoesNotThrow(() -> Taikai.builder()
        .namespace("com.enofex.taikai.spring")
        .spring(spring -> spring.controllers(controllers -> controllers
            .methodsShouldNotReturn(ClassCriteria.simpleNameMatching("InternalDTO"),
                Configuration.of(Namespace.IMPORT.WITH_TESTS, List.of(RestApi.class.getName(),
                    MvcController.class.getName(), ComposedController.class.getName())))))
        .build().check());
  }

  static class InternalDTO {
  }

  @Controller
  static class MvcController {
    InternalDTO result() {
      return null;
    }
  }

  @RestController
  static class RestApi {
    ResponseEntity<List<InternalDTO>> result() {
      return null;
    }
  }

  @RestController
  @Retention(RetentionPolicy.RUNTIME)
  @interface ApiController {
  }

  @ApiController
  static class ComposedController {
    InternalDTO[] result() {
      return null;
    }
  }

  @RestController
  static class SafeController {
    List<String> result() {
      return null;
    }
  }

  static class NonController {
    InternalDTO result() {
      return null;
    }
  }
}
