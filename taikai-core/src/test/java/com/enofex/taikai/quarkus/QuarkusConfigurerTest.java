package com.enofex.taikai.quarkus;

import static com.enofex.taikai.TaikaiRule.Configuration.defaultConfiguration;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import jakarta.inject.Inject;
import java.util.stream.Stream;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class QuarkusConfigurerTest {

  @Nested
  class FieldsNotUsingInject {

    static Stream<Named<Customizer<QuarkusConfigurer>>> rules() {
      return Stream.of(
          Named.of("noInjectionFields()",
              quarkus -> quarkus.noInjectionFields()),
          Named.of("noInjectionFields(Configuration)",
              quarkus -> quarkus.noInjectionFields(defaultConfiguration())));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldThrowAnExceptionWhenFieldsAreAnnotatedWithInject(
        Customizer<QuarkusConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(Injection.class)
          .quarkus(rule)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @Test
    void shouldThrowAnExceptionWhenFieldsAreAnnotatedWithInjectAndAParameterConstructor() {
      Taikai taikai = Taikai.builder()
          .classes(InjectionWithConstructor.class)
          .quarkus(QuarkusConfigurer::noInjectionFields)
          .build();

      assertThrows(AssertionError.class, taikai::check);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rules")
    void shouldNotThrowAnExceptionIfOnlyConstructorUsed(Customizer<QuarkusConfigurer> rule) {
      Taikai taikai = Taikai.builder()
          .classes(NoInjection.class)
          .quarkus(rule)
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }

  static class Injection {

    @Inject
    String name;

  }

  static class InjectionWithConstructor {

    @Inject
    String name;

    public InjectionWithConstructor(String name) {
      this.name = name;
    }

  }

  static class NoInjection {

    String name;

    public NoInjection(String name) {
      this.name = name;
    }

  }

  @Nested
  class Disable {

    @Test
    void shouldDisableQuarkusConfigurer() {
      Taikai taikai = Taikai.builder()
          .classes(Injection.class)
          .quarkus(quarkus -> {
            quarkus.noInjectionFields();
            quarkus.disable();
          })
          .build();

      assertDoesNotThrow(taikai::check);
    }
  }
}
