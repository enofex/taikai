package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
import com.enofex.taikai.configures.Customizer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

class ServicesConfigurerTest {

  @Nested
  class NamesShouldEndWithService {

    static Stream<Named<Customizer<ServicesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithService()",
              services -> services.namesShouldEndWithService()),
          Named.of("namesShouldEndWithService(Configuration)",
              services -> services.namesShouldEndWithService(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              services -> services.namesShouldMatch(".+Service")),
          Named.of("namesShouldMatch(String, Configuration)",
              services -> services.namesShouldMatch(".+Service", defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenServiceEndsWithService(Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenAnnotatedServiceDoesNotEndWithService(
        Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNaming.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithService {

    static Stream<Named<Customizer<ServicesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithService()",
              services -> services.shouldBeAnnotatedWithService()),
          Named.of("shouldBeAnnotatedWithService(Configuration)",
              services -> services.shouldBeAnnotatedWithService(defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithService(DescribedPredicate)",
              services -> services.shouldBeAnnotatedWithService(
                  ClassCriteria.nameMatching(".+Service"))),
          Named.of("shouldBeAnnotatedWithService(DescribedPredicate, Configuration)",
              services -> services.shouldBeAnnotatedWithService(
                  ClassCriteria.nameMatching(".+Service"), defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenAnnotatedWithService(Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenServiceMissingAnnotation(Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationService.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenServiceInterfaceIsNotAnnotated(Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(EmailService.class, JavaMailEmailService.class,
              ConsoleLoggingEmailService.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenServiceImplementationMissingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(EmailService.class, MissingAnnotationEmailService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldBeAnnotatedWithService))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldNotDependOnOtherServices {

    @Test
    void shouldNotThrowWhenServiceDoesNotDependOnOtherService() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldNotDependOnOtherServices))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenServiceDependsOnOtherService() {
      Taikai taikai = Taikai.builder()
          .classes(ServiceDependingOnOtherService.class, UserService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldNotDependOnOtherServices))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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

  @Nested
  class ShouldBePackagePrivate {

    static Stream<Named<Customizer<ServicesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBePackagePrivate()",
              services -> services.shouldBePackagePrivate()),
          Named.of("shouldBePackagePrivate(Configuration)",
              services -> services.shouldBePackagePrivate(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenServiceIsPackagePrivate(Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenServiceIsPublic(Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PublicService.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class UnrelatedClasses {

    static Stream<Named<Customizer<ServicesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithService()",
              services -> services.namesShouldEndWithService()),
          Named.of("shouldBeAnnotatedWithService()",
              services -> services.shouldBeAnnotatedWithService()),
          Named.of("shouldNotDependOnOtherServices()",
              services -> services.shouldNotDependOnOtherServices()),
          Named.of("shouldNotDependOnControllers()",
              services -> services.shouldNotDependOnControllers()),
          Named.of("shouldBePackagePrivate()",
              services -> services.shouldBePackagePrivate()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenPublicClassIsNotAnnotatedWithService(
        Customizer<ServicesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(PublicHelper.class)
          .spring(spring -> spring.services(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ServiceInterfaceWithMultipleImplementations {

    @Test
    void shouldNotThrowWhenAllRulesAreAppliedToServiceInterface() {
      Taikai taikai = Taikai.builder()
          .classes(EmailService.class, JavaMailEmailService.class,
              ConsoleLoggingEmailService.class)
          .spring(spring -> spring.services(services -> services
              .shouldBeAnnotatedWithService()
              .shouldNotDependOnControllers()
              .namesShouldEndWithService()))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Service
  static class UserService {
    // valid service
  }

  @Service
  static class InvalidNaming {

  }

  @Service
  public static class PublicService {

  }

  public static class PublicHelper {

  }

  static class MissingAnnotationService {

  }

  interface EmailService {

    void send(String to, String subject, String content);
  }

  @Service
  static class JavaMailEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String content) {
    }
  }

  @Service
  static class ConsoleLoggingEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String content) {
    }
  }

  static class MissingAnnotationEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String content) {
    }
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

  @Service
  static class ServiceDependingOnOtherService {

    private final UserService userService;

    ServiceDependingOnOtherService(UserService userService) {
      this.userService = userService;
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableServicesConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNaming.class)
          .spring(spring -> spring.services(svc -> {
            svc.namesShouldEndWithService();
            svc.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class FluentChaining {

    private ConfigurerContext createContext() {
      return new ConfigurerContext(null, new Configurers());
    }

    @Test
    void shouldReturnSameConfigurerFromEveryMethod() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());

      assertAll(
          () -> assertSame(configurer, configurer.namesShouldEndWithService()),
          () -> assertSame(configurer,
              configurer.namesShouldEndWithService(defaultConfiguration())),
          () -> assertSame(configurer, configurer.namesShouldMatch(".+Service")),
          () -> assertSame(configurer,
              configurer.namesShouldMatch(".+Service", defaultConfiguration())),
          () -> assertSame(configurer, configurer.shouldBeAnnotatedWithService()),
          () -> assertSame(configurer,
              configurer.shouldBeAnnotatedWithService(defaultConfiguration())),
          () -> assertSame(configurer,
              configurer.shouldBeAnnotatedWithService(ClassCriteria.nameMatching(".+Service"))),
          () -> assertSame(configurer, configurer.shouldBeAnnotatedWithService(
              ClassCriteria.nameMatching(".+Service"), defaultConfiguration())),
          () -> assertSame(configurer, configurer.shouldNotDependOnOtherServices()),
          () -> assertSame(configurer,
              configurer.shouldNotDependOnOtherServices(defaultConfiguration())),
          () -> assertSame(configurer, configurer.shouldNotDependOnControllers()),
          () -> assertSame(configurer,
              configurer.shouldNotDependOnControllers(defaultConfiguration())),
          () -> assertSame(configurer, configurer.disable()));
    }
  }

  @Nested
  class MetaAnnotated {

    @Test
    void shouldNotThrowWhenServiceIsMetaAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(MetaAnnotatedService.class)
          .spring(spring -> spring.services(ServicesConfigurer::namesShouldEndWithService))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMetaAnnotatedServiceNameDoesNotMatch() {
      Taikai taikai = Taikai.builder()
          .classes(MetaAnnotatedBadName.class)
          .spring(spring -> spring.services(ServicesConfigurer::namesShouldEndWithService))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenMetaAnnotatedServiceIsAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(MetaAnnotatedService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldBeAnnotatedWithService))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenMetaAnnotatedServiceDoesNotDependOnControllers() {
      Taikai taikai = Taikai.builder()
          .classes(MetaAnnotatedService.class)
          .spring(spring -> spring.services(ServicesConfigurer::shouldNotDependOnControllers))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE)
  @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
  @Service
  @interface CustomService {
  }

  @CustomService
  static class MetaAnnotatedService {
  }

  @CustomService
  static class MetaAnnotatedBadName {
  }
}
