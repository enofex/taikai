package com.enofex.taikai.metrics;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.function.Executable;

/**
 * Checks the metrics rules against fixture classes. The packages are imported once and shared, as
 * imported classes do not change.
 */
final class MetricsChecks {

  private static final Map<String, JavaClasses> PACKAGES = new ConcurrentHashMap<>();

  private MetricsChecks() {
  }

  static JavaClasses classesOf(String packageName) {
    return PACKAGES.computeIfAbsent(packageName,
        name -> new ClassFileImporter().importPackages(name));
  }

  static Executable check(String packageName, Customizer<MetricsConfigurer> customizer) {
    return check(customizer, classesOf(packageName));
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
