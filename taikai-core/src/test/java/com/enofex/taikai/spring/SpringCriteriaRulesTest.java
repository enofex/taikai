package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class SpringCriteriaRulesTest {

  @Nested
  class ControllersShouldBeAnnotatedWithRestController {

    @Test
    void shouldNotThrowWhenSelectedClassesAreRestControllers() {
      Taikai taikai = Taikai.builder()
          .classes(OrderApi.class, ApiContract.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching(".*Api.*"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotRestControllers() {
      Taikai taikai = Taikai.builder()
          .classes(PlainApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching(".*Api"))))
          .build();

      AssertionError failure = assertThrows(AssertionError.class, taikai::check);
      assertTrue(failure.getMessage().contains(PlainApi.class.getName()));
    }

    @Test
    void shouldHonorConfiguredClasses() {
      Taikai taikai = Taikai.builder()
          .classes(PlainApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithRestController(ClassCriteria.nameMatching(".*Api"),
                  Configuration.of(new ClassFileImporter().importClasses(OrderApi.class)))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ControllersShouldBeAnnotatedWithController {

    @Test
    void shouldNotThrowWhenSelectedClassesAreControllers() {
      Taikai taikai = Taikai.builder()
          .classes(PageApi.class, OrderApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithController(ClassCriteria.inPackage("com.enofex.taikai.spring")
                  .and(ClassCriteria.nameMatching(".*Api")))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotControllers() {
      Taikai taikai = Taikai.builder()
          .classes(PlainApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithController(ClassCriteria.nameMatching(".*Api"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ControllersShouldBeAnnotatedWithValidated {

    @Test
    void shouldNotThrowWhenSelectedControllersAreValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedQueryApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*QueryApi"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedControllersAreNotValidated() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedQueryApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*QueryApi"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldIgnoreControllersNotMatchingCriteria() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedQueryApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldBeAnnotatedWithValidated(ClassCriteria.inPackage("com.company.web.."))))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ControllersShouldNotBeAnnotatedWithValidated {

    @Test
    void shouldNotThrowWhenSelectedControllersAreNotValidated() {
      Taikai taikai = Taikai.builder()
          .classes(UnvalidatedQueryApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldNotBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*QueryApi"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedControllersAreValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedQueryApi.class)
          .spring(spring -> spring.controllers(controllers -> controllers
              .shouldNotBeAnnotatedWithValidated(ClassCriteria.annotatedWith(RestController.class))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ServicesShouldBeAnnotatedWithService {

    @Test
    void shouldNotThrowWhenSelectedClassesAreServices() {
      Taikai taikai = Taikai.builder()
          .classes(BillingManager.class, ManagerContract.class)
          .spring(spring -> spring.services(services -> services
              .shouldBeAnnotatedWithService(ClassCriteria.nameMatching(".*Manager.*"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotServices() {
      Taikai taikai = Taikai.builder()
          .classes(AuditManager.class)
          .spring(spring -> spring.services(services -> services
              .shouldBeAnnotatedWithService(ClassCriteria.nameMatching(".*Manager"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class RepositoriesShouldBeAnnotatedWithRepository {

    @Test
    void shouldNotThrowWhenSelectedClassesAreRepositories() {
      Taikai taikai = Taikai.builder()
          .classes(CustomerStore.class)
          .spring(spring -> spring.repositories(repositories -> repositories
              .shouldBeAnnotatedWithRepository(ClassCriteria.nameMatching(".*Store"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotRepositories() {
      Taikai taikai = Taikai.builder()
          .classes(InvoiceStore.class)
          .spring(spring -> spring.repositories(repositories -> repositories
              .shouldBeAnnotatedWithRepository(ClassCriteria.nameMatching(".*Store"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class PropertiesShouldBeAnnotatedWithConfigurationProperties {

    @Test
    void shouldNotThrowWhenSelectedClassesAreConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(MailSettings.class)
          .spring(spring -> spring.properties(properties -> properties
              .shouldBeAnnotatedWithConfigurationProperties(ClassCriteria.nameMatching(".*Settings"))))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenSelectedClassesAreNotConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(CacheSettings.class)
          .spring(spring -> spring.properties(properties -> properties
              .shouldBeAnnotatedWithConfigurationProperties(ClassCriteria.nameMatching(".*Settings"))))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @RestController
  static class OrderApi {
  }

  @Controller
  static class PageApi {
  }

  static class PlainApi {
  }

  interface ApiContract {
  }

  @RestController
  @Validated
  static class ValidatedQueryApi {

    public String find(@RequestParam @NotNull String id) {
      return id;
    }
  }

  @RestController
  static class UnvalidatedQueryApi {

    public String find(@RequestParam @NotNull String id) {
      return id;
    }
  }

  @Service
  static class BillingManager {
  }

  static class AuditManager {
  }

  interface ManagerContract {
  }

  @Repository
  static class CustomerStore {
  }

  static class InvoiceStore {
  }

  @ConfigurationProperties("mail")
  record MailSettings(String host) {
  }

  record CacheSettings(int size) {
  }
}
