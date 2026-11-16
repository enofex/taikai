package com.enofex.taikai.java;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.provider.Arguments;

/**
 * Checks the java rules against fixture classes.
 */
final class JavaChecks {

  private static final Map<List<Class<?>>, JavaClasses> IMPORTED = new ConcurrentHashMap<>();

  private JavaChecks() {
  }

  static JavaClasses imported(Class<?>... types) {
    return IMPORTED.computeIfAbsent(List.of(types),
        key -> new ClassFileImporter().importClasses(key));
  }

  static void assertReportsViolations(Customizer<JavaConfigurer> rule, Class<?> compliant,
      Class<?> violating) {
    assertDoesNotThrow(taikai(rule, compliant)::check);
    assertThrows(AssertionError.class, taikai(rule, violating)::check);
  }

  static Arguments overload(String name, Customizer<JavaConfigurer> rule, Class<?> compliant,
      Class<?> violating) {
    return Arguments.of(rule(name, rule), compliant, violating);
  }

  static Arguments namingOverload(String name, Customizer<NamingConfigurer> rule,
      Class<?> compliant, Class<?> violating) {
    return overload(name, java -> java.naming(rule), compliant, violating);
  }

  static Arguments importsOverload(String name, Customizer<ImportsConfigurer> rule,
      Class<?> compliant, Class<?> violating) {
    return overload(name, java -> java.imports(rule), compliant, violating);
  }

  static Named<Customizer<JavaConfigurer>> rule(String name, Customizer<JavaConfigurer> rule) {
    return Named.of(name, rule);
  }

  private static Taikai taikai(Customizer<JavaConfigurer> rule, Class<?> fixture) {
    return Taikai.builder()
        .classes(imported(fixture))
        .java(rule)
        .build();
  }
}
