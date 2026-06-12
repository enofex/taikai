package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

class TransactionalConfigurerTest {

  @Nested
  class MethodsShouldBePublic {

    @Test
    void shouldNotThrowWhenTransactionalMethodIsPublic() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenTransactionalMethodIsPackagePrivate() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPackagePrivateTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenTransactionalMethodIsProtected() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithProtectedTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenTransactionalMethodIsPrivate() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPrivateTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenJakartaTransactionalMethodIsPrivate() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPrivateJakartaTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNonPublicMethodIsNotTransactional() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithoutTransactionalMethods.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::methodsShouldBePublic))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldNotBeUsedInControllers {

    @Test
    void shouldNotThrowWhenControllerIsNotTransactional() {
      Taikai taikai = Taikai.builder()
          .classes(NonTransactionalController.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeUsedInControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenControllerIsAnnotatedWithTransactional() {
      Taikai taikai = Taikai.builder()
          .classes(TransactionalAnnotatedController.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeUsedInControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowWhenRestControllerDeclaresTransactionalMethod() {
      Taikai taikai = Taikai.builder()
          .classes(RestControllerWithTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeUsedInControllers))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenTransactionalIsUsedOutsideControllers() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeUsedInControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForMethodsShouldBePublic() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              transactional -> transactional.methodsShouldBePublic(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldNotBeUsedInControllers() {
      Taikai taikai = Taikai.builder()
          .classes(NonTransactionalController.class)
          .spring(spring -> spring.transactional(
              transactional -> transactional.shouldNotBeUsedInControllers(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableTransactionalConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPrivateTransactionalMethod.class)
          .spring(spring -> spring.transactional(transactional -> {
            transactional.methodsShouldBePublic();
            transactional.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Service
  static class ServiceWithPublicTransactionalMethod {

    @Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceWithPackagePrivateTransactionalMethod {

    @Transactional
    void save() {
    }
  }

  @Service
  static class ServiceWithProtectedTransactionalMethod {

    @Transactional
    protected void save() {
    }
  }

  @Service
  static class ServiceWithPrivateTransactionalMethod {

    @Transactional
    private void save() {
    }
  }

  @Service
  static class ServiceWithPrivateJakartaTransactionalMethod {

    @jakarta.transaction.Transactional
    private void save() {
    }
  }

  @Service
  static class ServiceWithoutTransactionalMethods {

    private void helper() {
    }
  }

  @Controller
  static class NonTransactionalController {

    public void handle() {
    }
  }

  @Controller
  @Transactional
  static class TransactionalAnnotatedController {

  }

  @RestController
  static class RestControllerWithTransactionalMethod {

    @Transactional
    public void handle() {
    }
  }
}
