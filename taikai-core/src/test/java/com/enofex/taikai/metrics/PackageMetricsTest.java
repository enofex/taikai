package com.enofex.taikai.metrics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * The fixtures in {@code shop} form three packages: {@code app} depends on {@code api} and
 * {@code domain}, and {@code api} depends on {@code domain}.
 */
class PackageMetricsTest {

  private static final String SHOP = "com.enofex.taikai.metrics.shop";

  @Test
  void shouldAllowClassesUpToMax() {
    assertDoesNotThrow(check(SHOP, metrics -> metrics.packagesShouldNotExceedMaxClasses(2)));
  }

  @Test
  void shouldThrowWhenClassesExceedMax() {
    AssertionError error = assertThrows(AssertionError.class,
        check(SHOP, metrics -> metrics.packagesShouldNotExceedMaxClasses(1)));

    assertTrue(error.getMessage().contains(
        "Package %s.app contains 2 classes, max allowed: 1".formatted(SHOP)));
  }

  @Test
  void shouldAllowPackageDependenciesUpToMax() {
    assertDoesNotThrow(check(SHOP, metrics -> metrics.packagesShouldNotExceedMaxDependencies(2)));
  }

  @Test
  void shouldThrowWhenPackageDependenciesExceedMax() {
    AssertionError error = assertThrows(AssertionError.class,
        check(SHOP, metrics -> metrics.packagesShouldNotExceedMaxDependencies(1)));

    assertTrue(error.getMessage().contains(
        "Package %1$s.app depends on 2 packages (%1$s.api, %1$s.domain), max allowed: 1"
            .formatted(SHOP)));
    assertFalse(error.getMessage().contains("Package %s.api ".formatted(SHOP)));
  }

  @Test
  void shouldAllowRelativeVisibilityUpToMax() {
    assertDoesNotThrow(check(SHOP + ".app",
        metrics -> metrics.packagesShouldNotExceedMaxRelativeVisibility(0.5)));
  }

  @Test
  void shouldThrowWhenRelativeVisibilityExceedsMax() {
    AssertionError error = assertThrows(AssertionError.class, check(SHOP,
        metrics -> metrics.packagesShouldNotExceedMaxRelativeVisibility(0.5)));

    assertTrue(error.getMessage().contains(
        "Package %s.domain has a relative visibility of 1.00 (1 of 1 classes are public), "
            .formatted(SHOP) + "max allowed: 0.5"));
    assertFalse(error.getMessage().contains("Package %s.app ".formatted(SHOP)));
  }

  private static Executable check(String packageName, Customizer<MetricsConfigurer> customizer) {
    JavaClasses classes = new ClassFileImporter().importPackages(packageName);

    return Taikai.builder()
        .classes(classes)
        .metrics(customizer)
        .build()::check;
  }
}
