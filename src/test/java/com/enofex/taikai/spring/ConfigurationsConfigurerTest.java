package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

class ConfigurationsConfigurerTest {

  @Nested
  class NamesShouldEndWithConfiguration {

    @Test
    void shouldNotThrowWhenConfigurationClassEndsWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ValidAppConfiguration.class)
          .spring(spring -> spring.configurations(
              ConfigurationsConfigurer::namesShouldEndWithConfiguration))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenConfigurationClassDoesNotEndWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidAppConfig.class)
          .spring(spring -> spring.configurations(
              ConfigurationsConfigurer::namesShouldEndWithConfiguration))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldIgnoreSpringBootApplicationClasses() {
      Taikai taikai = Taikai.builder()
          .classes(DemoApplication.class)
          .spring(spring -> spring.configurations(
              ConfigurationsConfigurer::namesShouldEndWithConfiguration))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenConfigurationClassNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(ValidAppConfiguration.class)
          .spring(spring -> spring.configurations(
              cfg -> cfg.namesShouldMatch(".+Configuration")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenConfigurationClassNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidAppConfig.class)
          .spring(spring -> spring.configurations(
              cfg -> cfg.namesShouldMatch(".+Configuration")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Configuration
  static class ValidAppConfiguration {

  }

  @Configuration
  static class InvalidAppConfig {

  }

  @Configuration
  record RecordAppConfiguration() {

  }

  @SpringBootApplication
  static class DemoApplication {

  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithConfiguration() {
      Taikai taikai = Taikai.builder()
          .classes(ValidAppConfiguration.class)
          .spring(spring -> spring.configurations(
              cfg -> cfg.namesShouldEndWithConfiguration(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisableConfigurationsConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidAppConfig.class)
          .spring(spring -> spring.configurations(cfg -> {
            cfg.namesShouldEndWithConfiguration();
            cfg.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }
}
