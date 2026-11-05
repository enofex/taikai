package com.enofex.taikai.spring;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

class PropertiesConfigurerTest {

  @Nested
  class NamesShouldEndWithProperties {

    static Stream<Named<Customizer<PropertiesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithProperties()",
              props -> props.namesShouldEndWithProperties()),
          Named.of("namesShouldEndWithProperties(Configuration)",
              props -> props.namesShouldEndWithProperties(defaultConfiguration())),
          Named.of("namesShouldMatch(String)",
              props -> props.namesShouldMatch(".+Properties")),
          Named.of("namesShouldMatch(String, Configuration)",
              props -> props.namesShouldMatch(".+Properties", defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenClassEndsWithProperties(Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenAnnotatedClassDoesNotEndWithProperties(
        Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(InvalidConfig.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithValidated {

    static Stream<Named<Customizer<PropertiesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithValidated()",
              props -> props.shouldBeAnnotatedWithValidated()),
          Named.of("shouldBeAnnotatedWithValidated(Configuration)",
              props -> props.shouldBeAnnotatedWithValidated(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenConfigurationPropertiesAnnotatedWithValidated(
        Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ValidatedProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenConfigurationPropertiesMissingValidatedAnnotation(
        Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class ShouldBeAnnotatedWithConfigurationProperties {

    static Stream<Named<Customizer<PropertiesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeAnnotatedWithConfigurationProperties()",
              props -> props.shouldBeAnnotatedWithConfigurationProperties()),
          Named.of("shouldBeAnnotatedWithConfigurationProperties(Configuration)",
              props -> props.shouldBeAnnotatedWithConfigurationProperties(
                  defaultConfiguration())),
          Named.of("shouldBeAnnotatedWithConfigurationProperties(DescribedPredicate)",
              props -> props.shouldBeAnnotatedWithConfigurationProperties(
                  ClassCriteria.nameMatching(".+Properties"))),
          Named.of(
              "shouldBeAnnotatedWithConfigurationProperties(DescribedPredicate, Configuration)",
              props -> props.shouldBeAnnotatedWithConfigurationProperties(
                  ClassCriteria.nameMatching(".+Properties"), defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenAnnotatedWithConfigurationProperties(
        Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenPropertiesClassIsMissingAnnotation(Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(MissingAnnotationProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenPropertiesInterfaceIsNotAnnotated(
        Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(MailProperties.class, DefaultMailProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  @Nested
  class ShouldBeRecords {

    static Stream<Named<Customizer<PropertiesConfigurer>>> rules() {
      return Stream.of(
          Named.of("shouldBeRecords()",
              props -> props.shouldBeRecords()),
          Named.of("shouldBeRecords(Configuration)",
              props -> props.shouldBeRecords(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenConfigurationPropertiesIsRecord(Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(RecordApplicationProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertDoesNotThrow(taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowWhenConfigurationPropertiesIsNotRecord(Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(ApplicationProperties.class)
          .spring(spring -> spring.properties(rule))
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }
  }

  @Nested
  class UnrelatedClasses {

    static Stream<Named<Customizer<PropertiesConfigurer>>> rules() {
      return Stream.of(
          Named.of("namesShouldEndWithProperties()",
              props -> props.namesShouldEndWithProperties()),
          Named.of("shouldBeAnnotatedWithValidated()",
              props -> props.shouldBeAnnotatedWithValidated()),
          Named.of("shouldBeAnnotatedWithConfigurationProperties()",
              props -> props.shouldBeAnnotatedWithConfigurationProperties()),
          Named.of("shouldBeRecords()",
              props -> props.shouldBeRecords()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowWhenClassIsNotConfigurationProperties(
        Customizer<PropertiesConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(RandomUtility.class)
          .spring(spring -> spring.properties(rule))
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

  interface MailProperties {

    String host();
  }

  @ConfigurationProperties(prefix = "mail")
  static class DefaultMailProperties implements MailProperties {

    private String host;

    @Override
    public String host() {
      return this.host;
    }
  }

  @ConfigurationProperties(prefix = "record")
  record RecordApplicationProperties(String name, int port) {
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
