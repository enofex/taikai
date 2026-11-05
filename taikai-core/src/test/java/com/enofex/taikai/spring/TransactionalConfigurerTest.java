package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

class TransactionalConfigurerTest {

  @Nested
  class MethodsShouldBePublic {

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("methodsShouldBePublic()",
              transactional -> transactional.methodsShouldBePublic()),
          Named.of("methodsShouldBePublic(Configuration)",
              transactional -> transactional.methodsShouldBePublic(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenTransactionalMethodIsPublic(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenTransactionalMethodIsPrivate(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPrivateTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ServiceWithPackagePrivateTransactionalMethod.class,
        ServiceWithProtectedTransactionalMethod.class,
        ServiceWithPrivateJakartaTransactionalMethod.class})
    void shouldThrowWhenTransactionalMethodIsNotPublic(Class<?> service) {
      Taikai taikai = Taikai.builder()
          .classes(service)
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
  class ShouldNotBeSelfInvoked {

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldNotBeSelfInvoked()",
              transactional -> transactional.shouldNotBeSelfInvoked()),
          Named.of("shouldNotBeSelfInvoked(Configuration)",
              transactional -> transactional.shouldNotBeSelfInvoked(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenTransactionalMethodIsCalledFromSameClass(
        Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(classes = {ServiceWithSelfInvokedJakartaTransactionalMethod.class,
        ServiceCallingTransactionalMethodFromConstructor.class})
    void shouldThrowWhenTransactionalMethodIsSelfInvoked(Class<?> service) {
      Taikai taikai = Taikai.builder()
          .classes(service)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeSelfInvoked))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenTransactionalMethodIsCalledFromAnotherClass() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceCallingAnotherService.class, ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeSelfInvoked))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenSelfInvokedMethodIsNotTransactional() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithSelfInvokedPlainMethod.class)
          .spring(spring -> spring.transactional(
              TransactionalConfigurer::shouldNotBeSelfInvoked))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenTransactionalMethodIsNotSelfInvoked(
        Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ServiceWithPublicTransactionalMethod.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldNotBeUsedInControllers {

    static Stream<Named<Customizer<TransactionalConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldNotBeUsedInControllers()",
              transactional -> transactional.shouldNotBeUsedInControllers()),
          Named.of("shouldNotBeUsedInControllers(Configuration)",
              transactional -> transactional.shouldNotBeUsedInControllers(
                  defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenControllerIsNotTransactional(Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(NonTransactionalController.class)
          .spring(spring -> spring.transactional(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenControllerIsAnnotatedWithTransactional(
        Customizer<TransactionalConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(TransactionalAnnotatedController.class)
          .spring(spring -> spring.transactional(rule))
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

  @Service
  static class ServiceWithSelfInvokedTransactionalMethod {

    public void process() {
      save();
    }

    @Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceWithSelfInvokedJakartaTransactionalMethod {

    public void process() {
      save();
    }

    @jakarta.transaction.Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceCallingTransactionalMethodFromConstructor {

    ServiceCallingTransactionalMethodFromConstructor() {
      save();
    }

    @Transactional
    public void save() {
    }
  }

  @Service
  static class ServiceWithSelfInvokedPlainMethod {

    public void process() {
      save();
    }

    public void save() {
    }
  }

  @Service
  static class ServiceCallingAnotherService {

    private final ServiceWithPublicTransactionalMethod delegate;

    ServiceCallingAnotherService(ServiceWithPublicTransactionalMethod delegate) {
      this.delegate = delegate;
    }

    public void process() {
      this.delegate.save();
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
