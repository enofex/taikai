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
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

class ConfigurationsConfigurerTest {

  @Nested
  class NamesShouldEndWithConfiguration {

    static Stream<Named<Customizer<ConfigurationsConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithConfiguration()",
              cfg -> cfg.namesShouldEndWithConfiguration()),
          Named.of("namesShouldEndWithConfiguration(Configuration)",
              cfg -> cfg.namesShouldEndWithConfiguration(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              cfg -> cfg.namesShouldMatch(".+Configuration")),
          Named.of("namesShouldMatch(String, Configuration)",
              cfg -> cfg.namesShouldMatch(".+Configuration", defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenConfigurationClassEndsWithConfiguration(
        Customizer<ConfigurationsConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ValidAppConfiguration.class)
          .spring(spring -> spring.configurations(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenConfigurationClassDoesNotEndWithConfiguration(
        Customizer<ConfigurationsConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(InvalidAppConfig.class)
          .spring(spring -> spring.configurations(rule))
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

  @SpringBootApplication
  static class DemoApplication {

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
