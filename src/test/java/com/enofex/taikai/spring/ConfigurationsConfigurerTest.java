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
}
