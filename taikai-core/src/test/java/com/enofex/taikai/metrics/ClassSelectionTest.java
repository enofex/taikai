package com.enofex.taikai.metrics;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.configures.Customizer;
import com.enofex.taikai.metrics.ClassMetricsTest.Level1;
import com.enofex.taikai.metrics.ClassMetricsTest.Level2;
import com.enofex.taikai.metrics.ClassMetricsTest.Level3;
import com.enofex.taikai.metrics.ClassMetricsTest.MethodsWithParameters;
import com.enofex.taikai.metrics.ClassMetricsTest.OtherLevel2;
import com.enofex.taikai.metrics.shop.app.OrderService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * The rules for classes check only the selected classes, but keep counting all checked classes,
 * also the ones outside the selection.
 */
class ClassSelectionTest {

  @Test
  void shouldCountSuperclassesOutsideTheSelection() {
    AssertionError error = assertThrows(AssertionError.class, check(
        metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(
            ClassCriteria.nameMatching(".*Level3"), 1),
        Level1.class, Level2.class, Level3.class));

    assertTrue(error.getMessage().contains(
        "Class %s has an inheritance depth of 2, max allowed: 1".formatted(
            Level3.class.getName())));
  }

  @Test
  void shouldNotCheckClassesOutsideTheSelectionForInheritanceDepth() {
    assertDoesNotThrow(check(
        metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(
            ClassCriteria.nameMatching(".*Level1"), 0),
        Level1.class, Level2.class, Level3.class));
  }

  @Test
  void shouldCountSubclassesOutsideTheSelection() {
    AssertionError error = assertThrows(AssertionError.class, check(
        metrics -> metrics.classesShouldNotExceedMaxSubclasses(
            ClassCriteria.nameMatching(".*Level1"), 1),
        Level1.class, Level2.class, OtherLevel2.class));

    assertTrue(error.getMessage().contains(
        "Class %s has 2 subclasses, max allowed: 1".formatted(Level1.class.getName())));
  }

  @Test
  void shouldNotCheckClassesOutsideTheSelectionForSubclasses() {
    assertDoesNotThrow(check(
        metrics -> metrics.classesShouldNotExceedMaxSubclasses(
            ClassCriteria.nameMatching(".*Level2"), 0),
        Level1.class, Level2.class, OtherLevel2.class));
  }

  @Test
  void shouldCountDependenciesOnClassesOutsideTheSelection() {
    AssertionError error = assertThrows(AssertionError.class, check(
        metrics -> metrics.classesShouldNotExceedMaxDependencies(
            ClassCriteria.nameMatching(".*OrderService"), 2),
        shop()));

    assertTrue(error.getMessage().contains(
        "Class %s depends on 3 classes, max allowed: 2".formatted(OrderService.class.getName())));
  }

  @Test
  void shouldNotCheckClassesOutsideTheSelectionForDependencies() {
    assertDoesNotThrow(check(
        metrics -> metrics.classesShouldNotExceedMaxDependencies(
            ClassCriteria.nameMatching(".*OrderMapper"), 1),
        shop()));
  }

  @Test
  void shouldCheckMethodsOfSelectedClassesOnly() {
    assertDoesNotThrow(check(
        metrics -> metrics.methodsShouldNotExceedMaxParametersForClass(
            ClassCriteria.nameMatching(".*Level1"), 2),
        Level1.class, MethodsWithParameters.class));

    AssertionError error = assertThrows(AssertionError.class, check(
        metrics -> metrics.methodsShouldNotExceedMaxParametersForClass(
            ClassCriteria.nameMatching(".*MethodsWithParameters"), 2),
        Level1.class, MethodsWithParameters.class));

    assertTrue(error.getMessage().contains("has 3 parameters, max allowed: 2"));
  }

  @Test
  void shouldFailOnEmptySelectionWhenConfigured() {
    Taikai taikai = Taikai.builder()
        .classes(Level1.class, Level2.class)
        .failOnEmpty(true)
        .metrics(metrics -> metrics
            .classesShouldNotExceedMaxDependencies(ClassCriteria.nameMatching(".*Missing"), 1))
        .build();

    AssertionError error = assertThrows(AssertionError.class, taikai::check);

    assertTrue(error.getMessage().contains("failed to check any classes"));
  }

  @Test
  void shouldDescribeSelectedClasses() {
    Taikai taikai = Taikai.builder()
        .classes(Level1.class)
        .metrics(metrics -> metrics
            .classesShouldNotExceedMaxDependencies(ClassCriteria.nameMatching(".*Service"), 10)
            .classesShouldNotExceedMaxInheritanceDepth(ClassCriteria.nameMatching(".*Entity"), 1)
            .methodsShouldNotExceedMaxParametersForClass(
                ClassCriteria.nameMatching(".*Controller"), 4))
        .build();

    assertEquals(List.of(
            "Classes with names matching .*Service should not depend on more than 10 classes",
            "Classes with names matching .*Entity should not have an inheritance depth of more "
                + "than 1",
            "Methods declared in classes with names matching .*Controller should not have more "
                + "than 4 parameters"),
        taikai.rules().stream()
            .map(rule -> rule.archRule().getDescription())
            .toList());
  }

  private static JavaClasses shop() {
    return new ClassFileImporter().importPackages("com.enofex.taikai.metrics.shop");
  }

  private static Executable check(Customizer<MetricsConfigurer> customizer,
      Class<?>... classes) {
    return check(customizer, new ClassFileImporter().importClasses(classes));
  }

  private static Executable check(Customizer<MetricsConfigurer> customizer,
      JavaClasses classes) {
    return Taikai.builder()
        .classes(classes)
        .metrics(customizer)
        .build()::check;
  }
}
