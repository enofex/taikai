package com.enofex.taikai.spring;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

class PropertiesConfigurerTest {

  @Nested
  class NamesShouldEndWithProperties {

    @Test
    void shouldNotThrowWhenClassEndsWithProperties() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::namesShouldEndWithProperties))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldNotThrowWhenNonPropertiesClassIsNotAnnotated() {
      Taikai taikai = Taikai.builder()
          .classes(RandomUtility.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::namesShouldEndWithProperties))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenAnnotatedClassDoesNotEndWithProperties() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidConfig.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::namesShouldEndWithProperties))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithValidated {

    @Test
    void shouldNotThrowWhenConfigurationPropertiesAnnotatedWithValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedProperties.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenConfigurationPropertiesMissingValidatedAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNotAnnotatedWithConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(RandomUtility.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::shouldBeAnnotatedWithValidated))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithConfigurationProperties {

    @Test
    void shouldNotThrowWhenAnnotatedWithConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(
              PropertiesConfigurer::shouldBeAnnotatedWithConfigurationProperties))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenPropertiesClassIsMissingAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationProperties.class)
          .spring(spring -> spring.properties(
              PropertiesConfigurer::shouldBeAnnotatedWithConfigurationProperties))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenClassNameDoesNotMatchPattern() {
      Taikai taikai = Taikai.builder()
          .classes(RandomUtility.class)
          .spring(spring -> spring.properties(
              PropertiesConfigurer::shouldBeAnnotatedWithConfigurationProperties))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeRecords {

    @Test
    void shouldNotThrowWhenConfigurationPropertiesIsRecord() {
      Taikai taikai = Taikai.builder()
          .classes(RecordApplicationProperties.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::shouldBeRecords))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenConfigurationPropertiesIsNotRecord() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::shouldBeRecords))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldNotThrowWhenNonConfigurationPropertiesClassIsNotRecord() {
      Taikai taikai = Taikai.builder()
          .classes(RandomUtility.class)
          .spring(spring -> spring.properties(PropertiesConfigurer::shouldBeRecords))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @ConfigurationProperties(prefix = "app")
  static class ApplicationProperties {

    private String name;
  }

  @ConfigurationProperties(prefix = "app")
  @Validated
  static class ValidatedProperties {

    private String host;
  }

  @ConfigurationProperties(prefix = "invalid")
  static class InvalidConfig {

    private int port;
  }

  static class MissingAnnotationProperties {

    private boolean enabled;
  }

  static class RandomUtility {

    static void doSomething() {
    }
  }

  @ConfigurationProperties(prefix = "record")
  record RecordApplicationProperties(String name, int port) {
  }

  @Nested
  class NamesShouldMatch {

    @Test
    void shouldNotThrowWhenPropertiesNameMatchesRegex() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.namesShouldMatch(".+Properties")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenPropertiesNameDoesNotMatchRegex() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidConfig.class)
          .spring(spring -> spring.properties(
              props -> props.namesShouldMatch(".+Properties")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithConfigurationPropertiesByRegex {

    @Test
    void shouldNotThrowWhenMatchingClassIsAnnotatedWithConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.shouldBeAnnotatedWithConfigurationProperties(".+Properties")))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldThrowWhenMatchingClassMissingConfigurationPropertiesAnnotation() {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.shouldBeAnnotatedWithConfigurationProperties(".+Properties")))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ConfigurationOverloads {

    @Test
    void shouldSupportConfigurationForNamesShouldEndWithProperties() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.namesShouldEndWithProperties(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithValidated() {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedProperties.class)
          .spring(spring -> spring.properties(
              props -> props.shouldBeAnnotatedWithValidated(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeAnnotatedWithConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.shouldBeAnnotatedWithConfigurationProperties(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportRegexAndConfigurationForShouldBeAnnotatedWithConfigurationProperties() {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.shouldBeAnnotatedWithConfigurationProperties(".+Properties",
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @Test
    void shouldSupportConfigurationForShouldBeRecords() {
      Taikai taikai = Taikai.builder()
          .classes(RecordApplicationProperties.class)
          .spring(spring -> spring.properties(
              props -> props.shouldBeRecords(
                  com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration())))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class Disable {

    @Test
    void shouldDisablePropertiesConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(InvalidConfig.class)
          .spring(spring -> spring.properties(props -> {
            props.namesShouldEndWithProperties();
            props.disable();
          }))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }
}
