package com.enofex.taikai.logging;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Checks the logging rules against fixture classes.
 */
final class LoggingChecks {

  private LoggingChecks() {
  }

  static void assertReportsViolations(Customizer<LoggingConfigurer> rule, Class<?> compliant,
      Class<?> violating) {
    assertDoesNotThrow(taikai(rule, compliant)::check);
    assertThrows(AssertionError.class, taikai(rule, violating)::check);
  }

  static Arguments overload(String name, Customizer<LoggingConfigurer> rule, Class<?> compliant,
      Class<?> violating) {
    return Arguments.of(Named.of(name, rule), compliant, violating);
  }

  private static Taikai taikai(Customizer<LoggingConfigurer> rule, Class<?> fixture) {
    return Taikai.builder()
        .classes(fixture)
        .logging(rule)
        .build();
  }
}
