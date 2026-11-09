package com.enofex.taikai.metrics;

import com.enofex.taikai.Namespace;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.function.Executable;

/**
 * Checks the metrics rules against fixture classes.
 */
final class MetricsChecks {

  private MetricsChecks() {
  }

  static Executable check(String packageName, Customizer<MetricsConfigurer> customizer) {
    return check(customizer, Namespace.withTests(packageName));
  }

  static Executable check(Customizer<MetricsConfigurer> customizer, Class<?>... classes) {
    return check(customizer, new ClassFileImporter().importClasses(classes));
  }

  static Executable check(Customizer<MetricsConfigurer> customizer, JavaClasses classes) {
    return Taikai.builder()
        .classes(classes)
        .metrics(customizer)
        .build()::check;
  }
}
