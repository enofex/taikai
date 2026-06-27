package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.ConfigurerContext;
import com.enofex.taikai.configures.Configurers;
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
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenServiceNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(
              svc -> svc.namesShouldMatch(".+Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenServiceNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidNaming.class)
          .spring(spring -> spring.services(
              svc -> svc.namesShouldMatch(".+Service")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithServiceByRegex {

    @Test
    void shouldNotThrowWhenMatchingClassIsAnnotatedWithService() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(
              svc -> svc.shouldBeAnnotatedWithService(".+Service")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassMissingServiceAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationService.class)
          .spring(spring -> spring.services(
              svc -> svc.shouldBeAnnotatedWithService(".+Service")))
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

  @Service
  static class ServiceDependingOnOtherService {

    private final UserService userService;

    ServiceDependingOnOtherService(UserService userService) {
      this.userService = userService;
    }
  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithService() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(
              svc -> svc.namesShouldEndWithService(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithService() {
      Taikai taikai = Taikai.builder()
          .classes(UserService.class)
          .spring(spring -> spring.services(
              svc -> svc.shouldBeAnnotatedWithService(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
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
    void namesShouldEndWithServiceShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.namesShouldEndWithService());
    }

    @Test
    void namesShouldEndWithServiceWithConfigShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.namesShouldEndWithService(
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()));
    }

    @Test
    void namesShouldMatchShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.namesShouldMatch(".+Service"));
    }

    @Test
    void namesShouldMatchWithConfigShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.namesShouldMatch(".+Service",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()));
    }

    @Test
    void shouldBeAnnotatedWithServiceNoArgShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldBeAnnotatedWithService());
    }

    @Test
    void shouldBeAnnotatedWithServiceWithConfigShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldBeAnnotatedWithService(
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()));
    }

    @Test
    void shouldBeAnnotatedWithServiceWithRegexShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldBeAnnotatedWithService(".+Service"));
    }

    @Test
    void shouldBeAnnotatedWithServiceWithRegexAndConfigShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldBeAnnotatedWithService(".+Service",
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()));
    }

    @Test
    void shouldNotDependOnOtherServicesShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldNotDependOnOtherServices());
    }

    @Test
    void shouldNotDependOnOtherServicesWithConfigShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldNotDependOnOtherServices(
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()));
    }

    @Test
    void shouldNotDependOnControllersShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldNotDependOnControllers());
    }

    @Test
    void shouldNotDependOnControllersWithConfigShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(
          configurer.shouldNotDependOnControllers(
              com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration()));
    }

    @Test
    void disableShouldReturnConfigurer() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      org.junit.jupiter.api.Assertions.assertNotNull(configurer.disable());
    }

    @Test
    void chainingMultipleRulesShouldWork() {
      ServicesConfigurer configurer = new ServicesConfigurer(createContext());
      ServicesConfigurer result = configurer
          .namesShouldEndWithService()
          .shouldBeAnnotatedWithService()
          .shouldNotDependOnOtherServices()
          .shouldNotDependOnControllers();
      org.junit.jupiter.api.Assertions.assertSame(configurer, result);
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
    void shouldThrowWhenMetaAnnotatedServiceMissingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationService.class)
          .spring(spring -> spring.services(
              svc -> svc.shouldBeAnnotatedWithService(".+Service")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
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
