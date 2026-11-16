package com.enofex.taikai.metrics;

import static com.enofex.taikai.metrics.MetricsChecks.SHOP;
import static com.enofex.taikai.metrics.MetricsChecks.check;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enofex.taikai.ClassCriteria;
import com.enofex.taikai.MethodCriteria;
import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule.Configuration;
import com.enofex.taikai.metrics.fixtures.nesting.CustomerService;
import com.enofex.taikai.metrics.fixtures.shop.app.OrderService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.AbstractList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The rules for classes check only the selected classes, but keep counting all checked classes,
 * also the ones outside the selection.
 */
class ClassMetricsTest {

  private static final String NESTING = "com.enofex.taikai.metrics.fixtures.nesting";

  @Nested
  class ClassesShouldNotExceedMaxInheritanceDepth {

    @Test
    void shouldAllowInheritanceDepthUpToMax() {
      assertDoesNotThrow(check(metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(2),
          Level1.class, Level2.class, Level3.class));
    }

    @Test
    void shouldThrowWhenInheritanceDepthExceedsMax() {
      AssertionError error = assertThrows(AssertionError.class,
          check(metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(1),
              Level1.class, Level2.class, Level3.class));

      assertTrue(error.getMessage().contains(
          "Class %s has an inheritance depth of 2, max allowed: 1".formatted(
              Level3.class.getName())));
    }

    @Test
    void shouldNotCountSuperclassesThatAreNotChecked() {
      assertDoesNotThrow(check(metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(0),
          Level3.class, JdkList.class));
    }

    @Test
    void shouldNotCheckAnonymousClassesForInheritanceDepth() {
      assertDoesNotThrow(check(metrics -> metrics.classesShouldNotExceedMaxInheritanceDepth(2),
          Level1.class, Level2.class, Level3.class, new Level3() { }.getClass()));
    }

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
  }

  @Nested
  class ClassesShouldNotExceedMaxSubclasses {

    @Test
    void shouldNotCountAnonymousSubclasses() {
      assertDoesNotThrow(check(metrics -> metrics.classesShouldNotExceedMaxSubclasses(0),
          Operation.class, Operation.PLUS.getClass(), Operation.MINUS.getClass(),
          Handler.class, new Handler() { }.getClass()));
    }

    @Test
    void shouldAllowSubclassesUpToMax() {
      assertDoesNotThrow(check(metrics -> metrics.classesShouldNotExceedMaxSubclasses(2),
          Level1.class, Level2.class, OtherLevel2.class));
    }

    @Test
    void shouldThrowWhenSubclassesExceedMax() {
      AssertionError error = assertThrows(AssertionError.class,
          check(metrics -> metrics.classesShouldNotExceedMaxSubclasses(1),
              Level1.class, Level2.class, OtherLevel2.class));

      assertTrue(error.getMessage().contains(
          "Class %s has 2 subclasses, max allowed: 1".formatted(Level1.class.getName())));
    }

    @Test
    void shouldNotCountSubclassesThatAreExcluded() {
      JavaClasses classes = new ClassFileImporter()
          .importClasses(Level1.class, Level2.class, OtherLevel2.class);

      assertDoesNotThrow(check(metrics -> metrics.classesShouldNotExceedMaxSubclasses(1,
          Configuration.of(classes, List.of(OtherLevel2.class)))));
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
  }

  @Nested
  class MethodsShouldNotExceedMaxParameters {

    @Test
    void shouldAllowMethodParametersUpToMax() {
      assertDoesNotThrow(check(metrics -> metrics.methodsShouldNotExceedMaxParameters(3),
          MethodsWithParameters.class));
    }

    @Test
    void shouldThrowWhenMethodParametersExceedMax() {
      AssertionError error = assertThrows(AssertionError.class,
          check(metrics -> metrics.methodsShouldNotExceedMaxParameters(2),
              MethodsWithParameters.class));

      assertTrue(error.getMessage().contains(
          "Method %s.threeParameters(java.lang.String, java.lang.String, java.lang.String) has 3 "
              .formatted(MethodsWithParameters.class.getName())
              + "parameters, max allowed: 2"));
    }

    @Test
    void shouldCheckOnlyMethodsMatchingTheCriteria() {
      assertDoesNotThrow(check(metrics -> metrics.methodsShouldNotExceedMaxParameters(
          MethodCriteria.nameMatching("oneParameter"), 1), MethodsWithParameters.class));

      AssertionError error = assertThrows(AssertionError.class,
          check(metrics -> metrics.methodsShouldNotExceedMaxParameters(
              MethodCriteria.nameMatching("threeParameters"), 2), MethodsWithParameters.class));

      assertTrue(error.getMessage().contains(
          "Methods with names matching threeParameters should not have more than 2 parameters"));
      assertTrue(error.getMessage().contains("has 3 parameters, max allowed: 2"));
    }
  }

  @Nested
  class MethodsShouldNotExceedMaxParametersForClass {

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
  }

  @Nested
  class ClassesShouldNotExceedMaxDependencies {

    @Test
    void shouldCountDependenciesOnNestedClassesAsOneOnTheirTopLevelClass() {
      assertDoesNotThrow(
          check(NESTING, metrics -> metrics.classesShouldNotExceedMaxDependencies(2)));
    }

    @Test
    void shouldAddDependenciesOfNestedAndAnonymousClassesToTheirTopLevelClass() {
      AssertionError error = assertThrows(AssertionError.class,
          check(NESTING, metrics -> metrics.classesShouldNotExceedMaxDependencies(1)));

      assertTrue(error.getMessage().contains(
          "Class %s depends on 2 classes, max allowed: 1".formatted(
              CustomerService.class.getName())));
      assertFalse(error.getMessage().contains("$"));
    }

    @Test
    void shouldCountDependenciesOnClassesOutsideTheSelection() {
      AssertionError error = assertThrows(AssertionError.class, check(SHOP,
          metrics -> metrics.classesShouldNotExceedMaxDependencies(
              ClassCriteria.nameMatching(".*OrderService"), 2)));

      assertTrue(error.getMessage().contains(
          "Class %s depends on 3 classes, max allowed: 2".formatted(OrderService.class.getName())));
    }

    @Test
    void shouldNotCheckClassesOutsideTheSelectionForDependencies() {
      assertDoesNotThrow(check(SHOP,
          metrics -> metrics.classesShouldNotExceedMaxDependencies(
              ClassCriteria.nameMatching(".*OrderMapper"), 1)));
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
  }

  static class Level1 {

  }

  static class Level2 extends Level1 {

  }

  static class OtherLevel2 extends Level1 {

  }

  static class Level3 extends Level2 {

  }

  static class JdkList extends AbstractList<String> {

    @Override
    public String get(int index) {
      return "";
    }

    @Override
    public int size() {
      return 0;
    }
  }

  static class MethodsWithParameters {

    void oneParameter(String first) {
    }

    void threeParameters(String first, String second, String third) {
    }
  }

  enum Operation {
    PLUS {
      @Override
      int apply(int left, int right) {
        return left + right;
      }
    },
    MINUS {
      @Override
      int apply(int left, int right) {
        return left - right;
      }
    };

    abstract int apply(int left, int right);
  }

  abstract static class Handler {

  }
}
